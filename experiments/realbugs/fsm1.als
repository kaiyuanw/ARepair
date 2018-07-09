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
  all start1, start2 : FSM.start | start1 = start2

  // FSM only has one stop state.
  all stop1, stop2 : FSM.stop | stop1 = stop2

  // DO YOU WANT TO ENFORCE THAT THERE IS ALWAYS A STOP STATE?
  some FSM.stop
  // Note: It's fine if the student does not state "one FSM.start" because it is implied.
}

// Part (b)
fact ValidStartAndStop {
  // A state cannot be both a start state and a stop state.
  FSM.start !in FSM.stop

  // No transition ends at the start state.
  // Fix: replace "s.transition !in FSM.start" with "FSM.start !in s.transition".
  all s: State | s.transition !in FSM.start

  // MV: If no transition then stop state
  // Fix: replace "=>" with "<=>".
  all s: State | s.transition = none => s in FSM.stop
}

// Part (c)
fact Reachability {
  // All states are reachable from the start state.
  State = FSM.start.*transition

  // The stop state is reachable from any state.
  all s: State | FSM.stop in s.*transition
}

run {} for 5
