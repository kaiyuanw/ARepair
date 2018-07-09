-- Automatically generated tests.

pred test20 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->-8->Element0 + Array0->-7->Element0 + Array0->-6->Element0 + Array0->-5->Element0 + Array0->-4->Element0 + Array0->-3->Element0 + Array0->-2->Element0 + Array0->-1->Element0 + Array0->0->Element0 + Array0->1->Element0 + Array0->2->Element0 + Array0->3->Element0 + Array0->4->Element0 + Array0->5->Element0 + Array0->6->Element0 + Array0->7->Element0
length = Array0->1
}}
}
run test20 for 3 expect 0
pred test36 {
some disj Array0: Array {
no Element
Array = Array0
no i2e
length = Array0->-1
}
}
run test36 for 3 expect 0
pred test32 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->4->Element0
length = Array0->5
}}
}
run test32 for 3 expect 1
pred test7 {
some disj Element0, Element1: Element {some disj Array0: Array {
Element = Element0 + Element1
Array = Array0
i2e = Array0->0->Element1 + Array0->1->Element0
length = Array0->-3 + Array0->0 + Array0->6
}}
}
run test7 for 3 expect 0
pred test6 {
some disj Element0, Element1, Element2: Element {some disj Array0, Array1: Array {
Element = Element0 + Element1 + Element2
Array = Array0 + Array1
i2e = Array0->0->Element2 + Array0->1->Element1 + Array0->2->Element0 + Array0->2->Element2 + Array0->3->Element0 + Array0->3->Element2 + Array0->4->Element0 + Array0->4->Element2 + Array0->5->Element0 + Array0->5->Element2 + Array1->1->Element0 + Array1->1->Element1 + Array1->3->Element1
length = Array0->6 + Array1->0
}}
}
run test6 for 3 expect 0
pred test29 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->0->Element0 + Array0->1->Element0 + Array0->2->Element0 + Array0->3->Element0 + Array0->4->Element0 + Array0->5->Element0
length = Array0->6
}}
}
run test29 for 3 expect 1
pred test24 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->3->Element0
length = Array0->4
}}
}
run test24 for 3 expect 1
pred test3 {
some disj Array0: Array {
no Element
Array = Array0
no i2e
length = Array0->0
}
}
run test3 for 3 expect 1
pred test1 {
some disj Element0, Element1: Element {some disj Array0: Array {
Element = Element0 + Element1
Array = Array0
i2e = Array0->0->Element1 + Array0->1->Element0 + Array0->2->Element0 + Array0->3->Element0 + Array0->4->Element0 + Array0->5->Element0 + Array0->6->Element0
length = Array0->7
}}
}
run test1 for 3 expect 1
pred test27 {
some disj Element0, Element1: Element {some disj Array0: Array {
Element = Element0 + Element1
Array = Array0
i2e = Array0->0->Element1 + Array0->1->Element0 + Array0->2->Element0 + Array0->3->Element0 + Array0->4->Element0 + Array0->5->Element1 + Array0->6->Element0
length = Array0->7
}}
}
run test27 for 3 expect 1
pred test12 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->0->Element0 + Array0->1->Element0
length = Array0->3
NoConflict[]
}}
}
run test12 for 3 expect 1
pred test10 {
some disj Element0, Element1, Element2: Element {some disj Array0: Array {
Element = Element0 + Element1 + Element2
Array = Array0
i2e = Array0->0->Element0 + Array0->0->Element1 + Array0->0->Element2
length = Array0->5
NoConflict[]
}}
}
run test10 for 3 expect 0
pred test11 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->1->Element0
length = Array0->5
NoConflict[]
}}
}
run test11 for 3 expect 1
pred test33 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->1->Element0 + Array0->2->Element0 + Array0->3->Element0 + Array0->6->Element0
length = Array0->0
}}
}
run test33 for 3 expect 0
pred test22 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->-8->Element0 + Array0->-7->Element0 + Array0->-6->Element0 + Array0->-5->Element0 + Array0->-4->Element0 + Array0->-3->Element0 + Array0->-2->Element0 + Array0->-1->Element0
length = Array0->0
}}
}
run test22 for 3 expect 0
pred test25 {
some disj Element0, Element1: Element {some disj Array0: Array {
Element = Element0 + Element1
Array = Array0
i2e = Array0->0->Element1 + Array0->1->Element0 + Array0->2->Element0
length = Array0->3
}}
}
run test25 for 3 expect 1
pred test19 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->-8->Element0 + Array0->-7->Element0 + Array0->-6->Element0 + Array0->-5->Element0 + Array0->-4->Element0 + Array0->-3->Element0 + Array0->-2->Element0 + Array0->-1->Element0 + Array0->0->Element0 + Array0->1->Element0 + Array0->2->Element0 + Array0->3->Element0 + Array0->4->Element0 + Array0->5->Element0 + Array0->6->Element0 + Array0->7->Element0
length = Array0->0
}}
}
run test19 for 3 expect 0
pred test13 {
some disj Array0: Array {
no Element
Array = Array0
no i2e
length = Array0->0
NoConflict[]
}
}
run test13 for 3 expect 1
pred test17 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->-8->Element0 + Array0->-7->Element0 + Array0->-6->Element0 + Array0->-5->Element0 + Array0->-4->Element0 + Array0->-3->Element0 + Array0->-2->Element0 + Array0->-1->Element0 + Array0->7->Element0
length = Array0->0
}}
}
run test17 for 3 expect 0
pred test30 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->0->Element0 + Array0->1->Element0 + Array0->2->Element0 + Array0->4->Element0 + Array0->5->Element0
length = Array0->3
}}
}
run test30 for 3 expect 0
pred test8 {
some disj Array0: Array {
no Element
Array = Array0
no i2e
no length
}
}
run test8 for 3 expect 0
pred test14 {
some disj Element0, Element1: Element {some disj Array0: Array {
Element = Element0 + Element1
Array = Array0
i2e = Array0->0->Element0 + Array0->0->Element1
length = Array0->4
NoConflict[]
}}
}
run test14 for 3 expect 0
pred test18 {
some disj Array0: Array {
no Element
Array = Array0
no i2e
length = Array0->-6
}
}
run test18 for 3 expect 0
pred test5 {
some disj Element0, Element1, Element2: Element {some disj Array0, Array1: Array {
Element = Element0 + Element1 + Element2
Array = Array0 + Array1
i2e = Array0->0->Element2 + Array0->1->Element1 + Array0->2->Element0 + Array0->2->Element2 + Array0->3->Element0 + Array0->3->Element2 + Array0->4->Element0 + Array0->4->Element2 + Array0->5->Element0 + Array0->5->Element2 + Array1->0->Element2 + Array1->1->Element0 + Array1->1->Element2 + Array1->5->Element1
length = Array0->6 + Array1->6
}}
}
run test5 for 3 expect 0
pred test2 {
some disj Element0, Element1: Element {some disj Array0: Array {
Element = Element0 + Element1
Array = Array0
i2e = Array0->0->Element1 + Array0->2->Element0 + Array0->4->Element0 + Array0->6->Element0
length = Array0->7
}}
}
run test2 for 3 expect 1
pred test23 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->0->Element0 + Array0->1->Element0 + Array0->2->Element0 + Array0->3->Element0
length = Array0->3
}}
}
run test23 for 3 expect 0
pred test28 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->0->Element0 + Array0->1->Element0 + Array0->2->Element0 + Array0->3->Element0
length = Array0->4
}}
}
run test28 for 3 expect 1
pred test35 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->0->Element0
length = Array0->3
}}
}
run test35 for 3 expect 1
pred test9 {
some disj Element0, Element1: Element {some disj Array0: Array {
Element = Element0 + Element1
Array = Array0
i2e = Array0->0->Element0 + Array0->0->Element1
length = Array0->1 + Array0->3
}}
}
run test9 for 3 expect 0
pred test26 {
some disj Element0, Element1: Element {some disj Array0: Array {
Element = Element0 + Element1
Array = Array0
i2e = Array0->0->Element1 + Array0->1->Element0 + Array0->2->Element1 + Array0->3->Element0 + Array0->4->Element0 + Array0->5->Element1 + Array0->6->Element0
length = Array0->7
}}
}
run test26 for 3 expect 1
pred test21 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->-8->Element0 + Array0->-7->Element0 + Array0->-6->Element0 + Array0->-5->Element0 + Array0->-4->Element0 + Array0->-3->Element0 + Array0->-2->Element0 + Array0->-1->Element0 + Array0->1->Element0 + Array0->2->Element0 + Array0->3->Element0 + Array0->6->Element0 + Array0->7->Element0
length = Array0->0
}}
}
run test21 for 3 expect 0
pred test31 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->0->Element0 + Array0->1->Element0 + Array0->5->Element0 + Array0->7->Element0
length = Array0->7
}}
}
run test31 for 3 expect 0
pred test34 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->1->Element0
length = Array0->1
}}
}
run test34 for 3 expect 0
pred test4 {

no Element
no Array
no i2e
no length

}
run test4 for 3 expect 0
pred test15 {
some disj Element0: Element {some disj Array0: Array {
Element = Element0
Array = Array0
i2e = Array0->-8->Element0 + Array0->-7->Element0 + Array0->-6->Element0 + Array0->-5->Element0 + Array0->-4->Element0 + Array0->-3->Element0 + Array0->-2->Element0 + Array0->-1->Element0 + Array0->0->Element0 + Array0->1->Element0 + Array0->2->Element0 + Array0->3->Element0 + Array0->4->Element0 + Array0->5->Element0 + Array0->6->Element0 + Array0->7->Element0
length = Array0->-8
}}
}
run test15 for 3 expect 0
pred test16 {
some disj Array0: Array {
no Element
Array = Array0
no i2e
length = Array0->-4
}
}
run test16 for 3 expect 0

-- Manually created tests.

pred test1001 {
some disj Element0, Element1, Element2: Element {some disj Array0: Array {
Element = Element0 + Element1 + Element2
Array = Array0
i2e = Array0->0->Element0 + Array0->0->Element1 + Array0->0->Element2
length = Array0->1
}}
}
run test1001 for 4 expect 1