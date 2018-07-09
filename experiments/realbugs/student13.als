sig List {
    header: set Node
}

sig Node {
    link: set Node,
    elem: set Int
}

fact allNodesBelongToAList { 
    all n:Node | some l:List | n in l.header.*link + l.header
}

// Correct
fact CardinalityConstraints{
    all l:List | lone l.header  
    all n:Node | lone n.link
    all n:Node | one n.elem
}

// Underconstraint. n.link could be empty so header=l1->n1, link=n1->n2
// should not be allowed.
pred Loop (This : List){
    // Fix: replace "in" with "=".
    no This.header || one n:This.header.*link | n.link in n
}

// Correct
pred Sorted(This: List){
    all n:This.header.*link | some n.link => n.elem <= n.link.elem
}

pred RepOk(This:List){
    Loop[This]
    Sorted[This]
}

// Correct
pred Count(This: List, x:Int, result:Int){
    RepOk[This]
    #{n: This.header.*link | n.elem in x} = result
}

abstract sig Boolean {}
one sig True, False extends Boolean {}

// Correct
pred Contains(This: List, x: Int, result: Boolean) {
    RepOk[This]
    x in This.header.*link.elem => result = True else result = False
}

fact IGNORE {
  one List
  List.header.*link = Node
}