package works.samazama.api

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

object V1 extends ApiV1 {
  implicit final val epochMillisEncoder: Encoder[Dto.EpochTime] = deriveEncoder[Dto.EpochTime]
}
