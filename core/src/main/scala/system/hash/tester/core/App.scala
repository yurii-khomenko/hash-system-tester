package system.hash.tester.core

import system.hash.tester.core.service.LoadService.startLoadTest

object App {
  def main(args: Array[String]): Unit = {
    startLoadTest()
    sys.exit(0)
  }
}