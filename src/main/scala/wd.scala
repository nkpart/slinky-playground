
package object wd {
  type SRequest = scalaz.http.request.Request[Stream]
  type SResponse = scalaz.http.response.Response[Stream]
}