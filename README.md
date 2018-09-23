# ARepair: A Repair Framework for Alloy

`ARepair` is a command line tool built on top of
[Alloy4.2](https://github.com/AlloyTools/org.alloytools.alloy).  Given
a faulty Alloy model (potentially with multiple faults) and a set of
AUnit tests that capture the desired model properties, `ARepair` is
able to repair the model so that all AUnit tests.  Internally,
`ARepair` has three main components: a fault locator that is able to
locate faults, a code generator that generate Alloy code fragments and
a synthesizer that implements different search strategies to search
for patches that make all tests pass.

# Requirements:

* Operating Systems
  - Linux (64 bit)
  - Mac OS (64 bit)

* Dependencies
  - Java 8: Must be installed and accessible from `PATH`.
  - Bash 4.4: Must be installed and accessible from `PATH`.
  - Maven >3.5.2: Must be installed and accessible from `PATH`.
  - Alloy 4.2: Must be in the classpath.  `ARepair` comes with
    Alloy4.2 under `libs/alloy.jar`.

# Installation:

## Clone ARepair repo

To run `ARepair`, use `git` to clone the repository.

```Shell
git clone git@github.com:kaiyuanw/ARepair.git
```

## Build ARepair

To build `ARepair`, Java 8 and Maven 3.5.2 or above must be installed.
Then, you can run `./arepair.sh --build` in Bash 4.4 to build
`ARepair`.

# Quick Start:

## Repair Faulty Alloy Models

To repair a faulty Alloy model, run
```Shell
./arepair.sh --run -m <arg> -t <arg> -s <arg> -c <arg> -g <arg> [-e] [-h <arg>] [-p <arg>] [-d <arg>]
```
or use the full argument name
```Shell
./arepair.sh --run --model-path <arg> --test-path <arg> --scope <arg> --minimum-cost <arg> --search-strategy <arg> [--enable-cache] [--max-try-per-hole <arg>] [--partition-num <arg>] [--max-try-per-depth <arg>]
```
 * `-m,--model-path`: This argument is required.  Pass the faulty
   Alloy model to repair as the argument.
 * `-t,--test-path`: This argument is required.  Pass the test file
   which contains AUnit tests that capture the desired properties of
   the expected model as the argument.  Note that
   [MuAlloy](https://github.com/kaiyuanw/MuAlloy) provides a way to
   generate mutant killing AUnit tests.  If you use MuAlloy, you still
   need to manually label whether a generated AUnit test is expected
   to be satisfiable or unsatisfiable.  You can also manually write
   AUnit tests following the
   [examples](https://github.com/kaiyuanw/ARepair/tree/master/experiments/test-suite).
 * `-s,--scope`: This argument is required.  Pass the Alloy scope for
   repairing the faulty Alloy model as the argument.  The scope should
   be larger than or equal to the minimum scope necessary to run all
   AUnit tests properly.
 * `-c,--minimum-cost`: This argument is required.  Pass the minimum
  cost/size of the expression to generate as the argument.  The
  internal code generator will generated expressions of the specified
  size for the deepest level of the suspicious AST.
 * `-g,--search-strategy`: This argument is required.  Pass the search
   strategy to use for the internal synthesizer as the argument.  The
   value should be either `all-combinations` or `base-choice`.
 * `-e,--enable-cache`: This argument is optional.  If this argument
   is specified, `ARepair` uses the hierarchical caching for repair.
   Otherwise, it does not.
 * `-h,--max-try-per-hole`: This argument is optional and is used when
   the search strategy is `base-choice`.  Pass the maximum number of
   candidate expressions to consider for each hole during repair as
   the argument.  If the argument is not specified, a default value of
   1000 is used.
 * `-p,--partition-num`: This argument is optional and is used when
   the search strategy is `all-combinations`.  Pass the number of
   partitions of the search space for a given hole as the argument.
   If the argument is not specified, a default value of 10 is used.
 * `-d,--max-try-per-depth`: This argument is optional and is used
   when the search strategy is `all-combinations`.  Pass the maximum
   number of combinations of candidate expressions to consider for
   each depth of holes during repair as the argument.  If the argument
   is not specified, a default value of 10000 is used.

For each run, the command reports, for each iteration: (1) fault
localization time; (2) the expression generation time; (3) the search
space; (4) whether the current iteration successfully make some
failing tests pass but preserve the passing test results; (5) whether
the fix comes from mutation-based fault localization or the
synthesizer; and (6) the model after the fix.  Finally, the command
reports the simplified fixed model if all tests pass.  Otherwise, the
command reports the latest state of the partially fixed model.

The fixed Alloy model will be stored under the project hidden
directory at `${project_dir}/experiments/results/${model_name}.als`.

## Included Models

We provide 38 real faulty Alloy models with labeled faults from
[Amalgam](http://cs.brown.edu/research/plt/dl/fse2017/EXAMPLES) and
graduate students.  These models are derived from 12 models and we
also provide the correct versions of the 12 models.  For each unique
model, we provide a test suite that capture the desired properties of
that model.  The `experiments/models` directory contains all 12
correct models.  The `experiments/realbugs` directory contains all 38
real faulty models.  The `experiments/test-suite` directory contains
all 12 AUnit test suites.  The example models are listed below:

 * `addr` models an address book.
 * `arr` models an array.
 * `balancedBST` models a balanced binary search tree.
 * `bempl` models a bad employee keeping the key of a lab.
 * `cd` models a Java class diagram.
 * `ctree` models a colored tree.
 * `dll` models an acyclic doubly-linked list.
 * `farmer` models the farmer river-crossing puzzle.
 * `fsm` models a finite state machine.
 * `grade` models how teaching assistants grade assignments.
 * `other` models a lab security policy.
 * `student`: models an acyclic singly-linked sorted list with
   counting and containment check.

To repair all 38 models run
```Shell
./arepair.sh --run-all
```
The default settings is declared in `model.sh` and you should be able
to reproduce the experimental results in the paper.  The logging files
contain statistics reported in the paper and are stored at
`experiments/meta` directory.  The final (partially) fixed models are
stored at `experiments/results` directory.

# License

MIT License, see `LICENSE` for more information.