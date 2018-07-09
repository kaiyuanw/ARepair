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

// Part (a)
fact Acyclic {
  // The list has no directed cycle along nxt, i.e., no node is
  // reachable from itself following one or more traversals along nxt.
  all n: Node | n !in n.^nxt
}

// Part (b)
pred UniqueElem() {
  // Unique nodes contain unique elements.
  all n1, n2: Node {
    n1 != n2 => n1.elem != n2.elem
  }
}

// Part (c)
pred Sorted() {
  // The list is sorted in ascending order (<=) along nxt.
  all n: Node {
    // Fix: replace "n.elem < n.nxt.elem" with "some n.nxt => n.elem <= n.nxt.elem".
    n.elem < n.nxt.elem
  }
}

// Part (d)
pred ConsistentPreAndNxt() {
  // For any node n1 and n2, if n1.nxt = n2, then n2.pre = n1; and vice versa.
  all n1, n2: Node {
    // Fix: replace "=>" with "<=>".
    n1.nxt = n2 => n2.pre = n1
  }
}

pred RepOk() {
  UniqueElem
  Sorted
  ConsistentPreAndNxt
}

run RepOk for 5
