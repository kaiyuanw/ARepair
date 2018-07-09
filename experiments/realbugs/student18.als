sig List {
    header : set Node
}

sig Node {
    link: set Node,
    elem: set Int
}

// Correct
fact CardinalityConstraints {
    all l: List | lone l.header
    all n: Node | lone n.link
    all n: Node | one n.elem
}

// Underconstraint.  Should disallow header = l1 -> n1, no link
pred Loop(This: List) {
    // Fix: replace "This.header.link" with "This.header".
    no This.header.link || one n: This.header.*link | n.link = n
}

// Overconstraint.  Should allow no n.link
pred Sorted(This: List) {
    // Fix: replace "one n.link && n.elem <= n.link.elem" with "no n.link || n.elem <= n.link.elem".
    all n: This.header.*link | one n.link && n.elem <= n.link.elem
}

pred RepOk(This: List) {
    Loop[This]
    Sorted[This]
}

// Underconstraint.  Should be && instead of ||
pred Count(This: List, x: Int, result: Int) {
    RepOk[This]
    // Fix: replace "||" with "&&".
    result = #{n: Node | n in This.header.*link || n.elem = x}
}

abstract sig Boolean {}
one sig True, False extends Boolean {}

// Correct
pred Contains(This: List, x: Int, result: Boolean) {
    RepOk[This]
    {some n: This.header.*link | n.elem = x} => result = True else result = False
}

fact IGNORE {
  one List
  List.header.*link = Node
}