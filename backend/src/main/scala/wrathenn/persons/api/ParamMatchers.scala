package wrathenn.persons.api

import org.http4s.dsl.impl.QueryParamDecoderMatcher

object PersonIdParamMatcher
  extends QueryParamDecoderMatcher[Long]("personId")
