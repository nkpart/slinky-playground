package scapps

sealed trait Verb

// Ze verbs.
case object Index extends Verb
case object Create extends Verb
case class Show(id: String) extends Verb
case class Update(id: String) extends Verb
case class Destroy(id: String) extends Verb

// Ze verbs for form requests
case object New extends Verb
case class Edit(id: String) extends Verb

// Represents resource root, and action
// eg. Base("breweries", Index) could represent GET /breweries
case class Action(b: String, v: Verb)

// The parent for a nested resource
// given: /beers/5/hops
// this is: Context("beers","5") is the parent for Action("hops", Index) 
case class Context(b: String, id: String) {
  def asAction = Action(b, Show(id))
}

// A full request: any number of parent contexts and a final action
case class RestRequest(contexts: List[Context], base: Action)
