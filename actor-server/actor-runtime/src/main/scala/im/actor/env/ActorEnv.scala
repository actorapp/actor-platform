package im.actor.env

import java.nio.file.{ Path, Paths }

object ActorEnv {

  def getAbsolutePath(path: Path): Path =
    if (path.isAbsolute) path else home.resolve(path).toAbsolutePath.normalize

  def getAbsolutePath(pathString: String): Path = getAbsolutePath(Paths.get(pathString))

  val home: Path = Paths.get(".").toAbsolutePath.normalize()

}
