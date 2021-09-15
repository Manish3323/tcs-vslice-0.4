package tcs.enc

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import csw.command.client.messages.TopLevelActorMessage
import csw.framework.models.CswContext
import csw.framework.scaladsl.ComponentHandlers
import csw.location.api.models.TrackingEvent
import csw.params.commands.CommandResponse._
import csw.params.commands.ControlCommand
import csw.params.core.generics.{Key, KeyType}
import csw.params.core.models.Id
import csw.params.events.{Event, EventKey, EventName, SystemEvent}
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem.TCS
import csw.time.core.models.UTCTime
import tcs.shared.SimulationUtil
import csw.params.core.models.Angle.double2angle

import scala.concurrent.ExecutionContextExecutor

/**
 * Domain specific logic should be written in below handlers.
 * This handlers gets invoked when component receives messages/commands from other component/entity.
 * For example, if one component sends Submit(Setup(args)) command to Tcshcd,
 * This will be first validated in the supervisor and then forwarded to Component TLA which first invokes validateCommand hook
 * and if validation is successful, then onSubmit hook gets invoked.
 * You can find more information on this here : https://tmtsoftware.github.io/csw/commons/framework.html
 */

object EncAssemblyHandlers {
  private val pkAssemblyPrefix             = Prefix(TCS, "PointingKernelAssembly")
  private val pkEnclosureDemandPosEventKey = EventKey(pkAssemblyPrefix, EventName("EnclosureDemandPosition"))
  private val pkEventKeys                  = Set(pkEnclosureDemandPosEventKey)

  // Actor to receive Assembly events
  private object EventHandlerActor {
    private val baseDemandKey: Key[Double] = KeyType.DoubleKey.make("BasePosition")
    private val capDemandKey: Key[Double]  = KeyType.DoubleKey.make("CapPosition")
    private val baseKey: Key[Double]       = KeyType.DoubleKey.make("base")
    private val capKey: Key[Double]        = KeyType.DoubleKey.make("cap")
    private val encTelPosEventName         = EventName("CurrentPosition")

    def make(cswCtx: CswContext): Behavior[Event] = {
      Behaviors.setup(ctx => new EventHandlerActor(ctx, cswCtx))
    }
  }

  private class EventHandlerActor(
      ctx: ActorContext[Event],
      cswCtx: CswContext,
      maybeCurrentBase: Option[Double] = None,
      maybeCurrentCap: Option[Double] = None
  ) extends AbstractBehavior[Event](ctx) {
    import EventHandlerActor._
    import cswCtx._
    private val log       = loggerFactory.getLogger
    private val publisher = cswCtx.eventService.defaultPublisher

    override def onMessage(msg: Event): Behavior[Event] = {
//      log.info(s"XXX received $msg")
      msg match {
        case e: SystemEvent =>
          if (e.eventKey == pkEnclosureDemandPosEventKey) {
            val demandBase = e(baseDemandKey).head
            val demandCap  = e(capDemandKey).head
            (maybeCurrentBase, maybeCurrentCap) match {
              case (Some(currentBase), Some(currentCap)) =>
                val (newBase, newCap) = getNextPos(demandBase, demandCap, currentBase, currentCap)
                val newEvent = SystemEvent(cswCtx.componentInfo.prefix, encTelPosEventName)
                  .add(baseKey.set(newBase))
                  .add(capKey.set(newCap))
//                log.info(s"XXX publish $newEvent")
                publisher.publish(newEvent)
                new EventHandlerActor(ctx, cswCtx, Some(newBase), Some(newCap))
              case _ =>
                new EventHandlerActor(ctx, cswCtx, Some(demandBase), Some(demandCap))
            }
          }
          else Behaviors.same
        case x =>
          log.error(s"Expected SystemEvent but got $x")
          Behaviors.same
      }
    }

    // Simulate converging on the (base, cap) demand
    // Note from doc: Mount accepts demands at 100Hz and enclosure accepts demands at 20Hz
    private def getNextPos(targetBase: Double, targetCap: Double, currentBase: Double, currentCap: Double): (Double, Double) = {
      val speed  = 1.15 // deg/sec
      val rate   = 20.0 // hz
      val factor = 2.0  // Speedup factor for test/demo
      (
        SimulationUtil.move(speed * factor, rate, targetBase.degree, currentBase.degree).toDegree,
        SimulationUtil.move(speed * factor, rate, targetCap.degree, currentCap.degree).toDegree
      )
    }
  }

}

class EncAssemblyHandlers(ctx: ActorContext[TopLevelActorMessage], cswCtx: CswContext) extends ComponentHandlers(ctx, cswCtx) {
  import EncAssemblyHandlers._
  import cswCtx._
  implicit val ec: ExecutionContextExecutor = ctx.executionContext
  private val log                           = loggerFactory.getLogger

  override def initialize(): Unit = {
    log.info("Initializing ENC assembly...")
    val subscriber   = cswCtx.eventService.defaultSubscriber
    val eventHandler = ctx.spawn(EventHandlerActor.make(cswCtx), "EncAssemblyEventHandler")
    subscriber.subscribeActorRef(pkEventKeys, eventHandler)
  }

  override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit = {}

  override def validateCommand(runId: Id, controlCommand: ControlCommand): ValidateCommandResponse = Accepted(runId)

  override def onSubmit(runId: Id, controlCommand: ControlCommand): SubmitResponse = Completed(runId)

  override def onOneway(runId: Id, controlCommand: ControlCommand): Unit = {}

  override def onShutdown(): Unit = {}

  override def onGoOffline(): Unit = {}

  override def onGoOnline(): Unit = {}

  override def onDiagnosticMode(startTime: UTCTime, hint: String): Unit = {}

  override def onOperationsMode(): Unit = {}

}
