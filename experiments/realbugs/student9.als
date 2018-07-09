sig List  {
    header: set Node
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

// Correct
pred Loop(This: List) {
    no This.header || one n: This.header.*link | n in n.link 
}

// Correct
pred Sorted(This: List) {
    all n: This.header.*link | some n.link => n.elem <= n.link.elem
}

pred RepOk(This: List) {
    Loop[This]
    Sorted[This]
}

// Correct
pred Count(This: List, x: Int, result: Int) {
    RepOk[This]
    #{n: This.header.*link | n.elem = x} = result 
}

abstract sig Boolean {}
one sig True, False extends Boolean {}

// Under constraint.  result could be True when the list does not contains x.
pred Contains(This: List, x: Int, result: Boolean) {
    RepOk[This]
    // Fix: replace "=>" with "<=>".
    {some n: This.header.*link | n.elem = x } => True = result
}

fact IGNORE {
  one List
  List.header.*link = Node
}