package works.samazama.config

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigSupport {
  private lazy val configuration = ConfigFactory.load()
  protected def config: Config = configuration
}

object AppConfig extends ConfigSupport {

  object http {
    lazy val interface: String = config.getString("app.http.interface")
    lazy val port: Int = config.getInt("app.http.port")
    lazy val apiPath: String = config.getString("app.http.api.path")
  }
}
