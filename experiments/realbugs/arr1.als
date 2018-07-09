sig Element {}
one sig Array {
  // Maps indexes to elements of Element.
  i2e: Int -> Element,
  // Represents the length of the array.
  length: Int
}
// Assume all elements are in the array.
fact Reachable {
  Element = Array.i2e[Int]
}

fact InBound {
  // All indexes should be greater than or equal to 0 and less than the array length.
  //Array.Int>=0 && Array.i2e<Array.length
  //all i:Int|(i>=0 and i<Array.length)
  // Should be "all i: Array.i2e.Element | ...".
  // Fix: replace "Int" with "Array.i2e.Element".
  all i:Int |i>=0 and i<Array.length
  // Array length should be greater than or equal to 0.
  Array.length>=0
  //Int [i]:i2e | i>=0
}

pred NoConflict() {
  // Each index maps to at most one element.
  all idx: Array.i2e.Element | lone Array.i2e[idx]
}
run NoConflict