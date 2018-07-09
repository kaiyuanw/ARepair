one sig BinaryTree {
  root: lone Node
}

sig Node {
  left, right: lone Node,
  elem: Int
}

// All nodes are in the tree.
fact Reachable {
  Node = BinaryTree.root.*(left + right)
}

fact Acyclic {
  all n:Node {
    // There are no directed cycles, i.e., a node is not reachable
    // from itself along one or more traversals of left or right.
    n not in n.^(left + right)

    // A node cannot have more than one parent
    lone n.~(left + right)

    // A node cannot have another node as both its left child
    // and right child.
    no n.left & n.right
  }
}

pred Sorted() {
  all n:Node {
    // All elements in the n's left subtree are smaller than the n's elem.
    // Fix: replace "n.^left" with "n.left.*(left + right)".
    all n2:n.^left | n2.elem < n.elem
    // All elements in the n's right subtree are bigger than the n's elem.
    // Fix: replace "n.^right" with "n.right.*(left + right)".
    all n2:n.^right | n2.elem > n.elem
  }
}

pred HasAtMostOneChild(n:Node) {
  // Node n has at most one child
  #n.(left+right) <= 1
}

fun Depth(n: Node): one Int {
  // The number of nodes from the tree's root to n.
  #n.*~(left+right)
}

pred Balanced() {
  // If n1 has at most one child and n2 has at most one child, then the
  // depths of n1 and n2 differ by at most 1.
  // Multiplying depth differences by the signum to get rid of negatives.
  // Is there an absolute value in alloy?
  all n1, n2: Node {
    // Fix: replace "<=>" with "=>".
    (HasAtMostOneChild[n1] && HasAtMostOneChild[n2]) <=> (mul[signum[minus[Depth[n1], Depth[n2]]], minus[Depth[n1], Depth[n2]]] <= 1)
  }
}

pred RepOk() {
  Sorted
  Balanced
}

run RepOk for 5
