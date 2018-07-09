one sig DLL {
  header: lone Node
}

sig Node {
  pre, nxt: lone Node,
  elem: Int
}

// All nodes are reachable from the header node.
fact Reachable {
  Node = DLL.header.*nxt
}

fact Acyclic {
  all n : Node {
    // The list has no directed cycle along nxt, i.e., no node is
    // reachable from itself following one or more traversals along nxt.
    // Fix: replace "nxt+pre" with "nxt".
    n not in n.^(nxt+pre)
    // n not in n.^pre
    // no n.pre & n.nxt
    // all n:Node|no n.header or some dl:
  }
}

pred UniqueElem() {
  // Unique nodes contain unique elements.
  // Fix: replace "all n:Node | n.pre.elem & n.nxt.elem = none" with "all n:Node | all n2:Node | n != n2 => n.elem != n2.elem".
  all n:Node | n.pre.elem & n.nxt.elem = none
}

pred Sorted() {
  // The list is sorted in ascending order (<=) along link.
  all n: DLL.header.*nxt | some n.nxt implies n.elem <= n.nxt.elem
}

pred ConsistentPreAndNxt() {
  //all n1, n2: Node {
  // For any node n1 and n2, if n1.link = n2, then n2.prev = n1; and vice versa.
  //{some n1.link=n2}  implies n2.prev = n1 else n2.link = n1
  //}
  // Fix: add "nxt = ~pre".
}

pred RepOk() {
  UniqueElem
  Sorted
  ConsistentPreAndNxt
}

run RepOk for 3