sig Element {}

one sig Array {
  // Maps indexes to elements of Element.
  i2e: Int -> Element,
  // Represents the length of the array.
  length: Int
}

// Assume all objects are in the array.
fact Reachable {
  Element = Array.i2e[Int]
}

fact InBound {
  // All indexes should be greater than or equal to 0
  // and less than the array length
  all i:Array.i2e.Element | i >= 0
  all i:Array.i2e.Element | i < Array.length

  // Array length should be greater than equal to 0,
  // but also since there is a one-to-one mapping from
  // index to element, we restrict the array length to the
  // number of elements.
  // Fix: replace "Array.length = #Element" with "Array.length >= 0".
  Array.length = #Element
}

pred NoConflict() {
  // Each index maps to at most on element
  all i:Array.i2e.Element | #Array.i2e[i] = 1
}

run NoConflict for 3