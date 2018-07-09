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

// Part (a)
fact Acyclic {
  all n : Node {
    // There are no directed cycles, i.e., a node is not reachable
    // from itself along one or more traversals of left or right.
    n !in n.^(left + right)
    // A node cannot have more than one parent.
    lone n.~(left + right)
    // A node cannot have another node as both its left child and
    // right child.
    no n.(left) & n.(right)
  }
}

// Part (b)
pred Sorted() {
  all n: Node {
    // All elements in the n's left subtree are smaller than the n's elem.
    // Fix: replace "some n.left =>n.left.elem<n.elem" with "all nl: n.left.*(left + right) | nl.elem < n.elem".
    some n.left =>n.left.elem<n.elem
    // All elements in the n's right subtree are bigger than the n's elem.
    // Fix: replace "some n.right =>n.right.elem>n.elem" with "all nr: n.right.*(left + right) | nr.elem > n.elem".
    some n.right =>n.right.elem>n.elem
  }
}

// Part (c)
pred HasAtMostOneChild(n: Node) {
  // Node n has at most one child.
  lone n.(left+right)
}

fun Depth(n: Node): one Int {
  // The number of nodes from the tree's root to n.
  // Fix: replace "(#BinaryTree.root.*(left+right))-(#n.(left+right))" with "#{n.*~(left + right)}".
  (#BinaryTree.root.*(left+right))-(#n.(left+right))
}

pred Balanced() {
  all n1, n2: Node {
    // If n1 has at most one child and n2 has at most one child,
    // then the depths of n1 and n2 differ by at most 1.
    // Fix: replace "lone (#n1.(left+right) - #n2.(left+right))" with "(HasAtMostOneChild[n1] && HasAtMostOneChild[n2]) => (let diff = minus[Depth[n1], Depth[n2]] | -1 <= diff && diff <= 1)".
    lone (#n1.(left+right) - #n2.(left+right))
  }
}

pred RepOk() {
  Sorted
  Balanced
}

run RepOk for 5