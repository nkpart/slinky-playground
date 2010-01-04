import org.specs._

import wd._

class Hi extends SpecificationWithJUnit {

  "'hello world' matches 'h.* w.*'" in {
     "hello world" must be matching("h.* f.*")
  }
}
