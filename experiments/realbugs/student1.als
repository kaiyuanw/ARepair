sig List {
    header: set Node
}
sig Node {
    link: set Node,
    elem: set Int
}

// Correct
fact CardinalityConstraints {
    all l: List | lone l.header
    all n: Node | lone n.link && one n.elem
}

// Correct
pred Loop(This: List) {
   all n: Node | n in This.header.*link
   #Node=0  || some n: This.header.*link | n.link = n
}

// Underconstraint.  Should be true if no n.link.
pred Sorted(This: List) {
    // Fix: replace "n.elem <= n.link.elem" with "some n.link => n.elem <= n.link.elem".
    all n: This.header.*link | n.elem <= n.link.elem
}

pred RepOk(This: List) {
    Loop[This]
    Sorted[This]
}

// Correct
pred Count(This: List, x: Int, result: Int) {
    RepOk[This]
    result = #{n:Node | n in This.header.*link && n.elem=x}
}

abstract sig Boolean {}
one sig True, False extends Boolean {}
// Correct
pred Contains(This: List, x: Int, result: Boolean) {
    RepOk[This]
    result = {some {n :Node | n in This.header.*link && n.elem=x} => True else False}
}

fact IGNORE {
  one List
  List.header.*link = Node
}