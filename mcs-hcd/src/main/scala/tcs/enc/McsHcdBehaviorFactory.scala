package tcs.mcs

import akka.actor.typed.scaladsl.ActorContext
import csw.framework.models.CswContext
import csw.framework.scaladsl.{ComponentBehaviorFactory, ComponentHandlers}
import csw.command.client.messages.TopLevelActorMessage

class McsHcdBehaviorFactory extends ComponentBehaviorFactory {

  override def handlers(ctx: ActorContext[TopLevelActorMessage], cswCtx: CswContext): ComponentHandlers =
    new McsHcdHandlers(ctx, cswCtx)

}
