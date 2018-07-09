sig List {
    header: set Node
} 

sig Node {
    link: set Node,
    elem: set Int
}

// Correct
fact CardinalityConstraints {
     all l:List |  lone l.header
     all n:Node | lone n.link
     all n: Node | one n.elem
     List.header.*link = Node
}

// Correct
pred Loop(This: List) {
     no This.header || one n: This.header.*link | n in n.^link
}

// Overconstraint.  Should allow no n.link
pred Sorted(This: List) {
     // Fix: replace "n.elem <= n.link.elem" with "some n.link => n.elem <= n.link.elem".
     no This.header || all n: This.header.*link | n.elem <= n.link.elem
}

pred RepOk(This: List) {
     Loop[This]
     Sorted[This]
}

// Correct
pred Count(This: List, x: Int, result: Int) {
    RepOk[This]
    result = #{n: This.header.*link | n.elem = x}
     
}

abstract sig Boolean {}
one sig True, False extends Boolean {}

// Correct
pred Contains(This: List, x: Int, result: Boolean) {
    RepOk[This]
    #(x & This.header.*link.elem) = 1 => result = True
    #(x & This.header.*link.elem) = 0 => result = False
}

fact IGNORE {
  one List
  List.header.*link = Node
}