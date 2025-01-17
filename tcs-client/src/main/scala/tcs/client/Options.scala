package tcs.client

import csw.params.commands.CommandName
import csw.params.core.models.ObsId
import csw.services.BuildInfo

case class Options(
    command: CommandName = CommandName("SlewToTarget"),
    ra: Option[String] = None,
    dec: Option[String] = None,
    frame: Option[String] = None,
    pmx: Double = 0.0,
    pmy: Double = 0.0,
    x: Double = 0.0,
    y: Double = 0.0,
    obsId: Option[ObsId] = None,
    subscribeToEvents: Boolean = false
)

object Options {
  private val defaults = Options()

  // Parser for the command line options
  private val parser = new scopt.OptionParser[Options]("tcs-client") {
    head("tcs-client", BuildInfo.version)

    opt[String]('c', "command") valueName "<command>" action { (x, c) =>
      c.copy(command = CommandName(x))
    } text s"The command to send to the pk assembly (One of: SlewToTarget, SetOffset. Default: ${defaults.command.name})"

    opt[String]('r', "ra") valueName "<RA>" action { (x, c) =>
      c.copy(ra = Some(x))
    } text s"The RA coordinate for the command in the format hh:mm:ss.sss"

    opt[String]('d', "dec") valueName "<Dec>" action { (x, c) =>
      c.copy(dec = Some(x))
    } text s"The Dec coordinate for the command in the format dd:mm:ss.sss"

    opt[String]('f', "frame") valueName "<frame>" action { (x, c) =>
      c.copy(frame = Some(x))
    } text s"The frame of refererence for RA, Dec: (default: ${defaults.frame})"

    opt[Double]("pmx") valueName "<pmx>" action { (x, c) =>
      c.copy(pmx = x)
    } text s"The primary motion x value: (default: ${defaults.pmx})"

    opt[Double]("pmy") valueName "<pmy>" action { (x, c) =>
      c.copy(pmy = x)
    } text s"The primary motion y value: (default: ${defaults.pmy})"

    opt[Double]('x', "x") valueName "<x>" action { (x, c) =>
      c.copy(x = x)
    } text s"The x offset in arcsec: (default: ${defaults.x})"

    opt[Double]('y', "y") valueName "<y>" action { (x, c) =>
      c.copy(y = x)
    } text s"The y offset in arcsec: (default: ${defaults.y})"

    opt[String]('o', "obsId") valueName "<id>" action { (x, c) =>
      c.copy(obsId = Some(ObsId(x)))
    } text s"The observation id: (default: ${defaults.obsId})"

    // TODO: make value a wildcard for psubscribe
    opt[Boolean]('s', "subscribe") action { (x, c) =>
      c.copy(subscribeToEvents = true)
    } text s"Subscribe to all events published here"

    help("help")
    version("version")
  }

  private def supportedCommands = Set("SlewToTarget", "SetOffset")

  //noinspection SameParameterValue
  private def error(msg: String): Unit = {
    println(msg)
    System.exit(1)
  }

  private def checkOptions(options: Options): Unit = {
    if (!supportedCommands.contains(options.command.name))
      error(s"Unsupported pk assembly command ${options.command}")
  }

  def parse(args: Array[String], run: Options => Unit): Unit = {
    parser.parse(args, Options()) match {
      case Some(options) =>
        try {
          checkOptions(options)
          run(options)
        }
        catch {
          case e: Throwable =>
            e.printStackTrace()
            System.exit(1)
        }
      case None => System.exit(1)
    }
  }

}
