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
  #FSM.start = 1
  // FSM only has one stop state.
  #FSM.stop = 1
}

// Part (b)
fact ValidStartAndStop {
  // A state cannot be both a start state and a stop state.
  FSM.start != FSM.stop
  // No transition ends at the start state.
  // Fix: replace "!=" with "!in".  If we consider all facts together, then this fix is sufficient.
  all s:State | FSM.start != s.transition
  // No transition begins at the stop state.
  no FSM.stop.transition
}

// Part (c)
fact Reachability {
  // All states are reachable from the start state.
  State = FSM.start.*transition
  // The stop state is reachable from any state.
  all s:(State - FSM.stop) | FSM.stop in s.*transition
}

run {} for 5
