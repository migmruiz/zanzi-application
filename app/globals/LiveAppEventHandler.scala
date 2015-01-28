package globals

import filters.Authorization
import play.api.mvc.WithFilters

object LiveAppEventHandler extends WithFilters(Authorization) {

}