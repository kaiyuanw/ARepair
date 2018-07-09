one sig FSM {
  start: set State,
  stop: set State
}

sig State {
  transition: set State
}

// Part (a)
fact OneStartAndStop {
  // FSM only has one start state.
  one FSM.start
  // FSM only has one stop state.
  one FSM.stop
}

// Part (b)
fact ValidStartAndStop {
  // A state cannot be both a start state and a stop state.
  no FSM.start & FSM.stop
  // No transition ends at the start state.
  no transition.(FSM.start)
  // No transition begins at the stop state.
  no (FSM.stop).transition
}

// Part (c)
fact Reachability {
  // All states are reachable from the start state.
  all n: State | n in FSM.start.*transition
  // The stop state is reachable from any state.
  all n: State | FSM.stop in n.*transition 
}

run {} for 5
