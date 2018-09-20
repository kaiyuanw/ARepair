#!/usr/bin/env bash

_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

EXPERIMENT_DIR="${_PROJECT_DIR}/experiments"
MODEL_DIR="${EXPERIMENT_DIR}/models"
REAL_BUG_DIR="${EXPERIMENT_DIR}/realbugs"
RESULT_DIR="${EXPERIMENT_DIR}/results"
TEST_DIR="${EXPERIMENT_DIR}/test-suite"
META_DIR="${EXPERIMENT_DIR}/meta"
HIDDEN_DIR="${_PROJECT_DIR}/.hidden"
FIXED_MODEL_PATH="${HIDDEN_DIR}/fix.als"

JAR_PATHS="${_PROJECT_DIR}/target/arepair-1.0-jar-with-dependencies.jar:${_PROJECT_DIR}/libs/aparser-1.0.jar:${_PROJECT_DIR}/libs/alloy.jar"

# Constant
BASE_CHOICE="base-choice"
ALL_COMBINATIONS="all-combinations"

# Setting
### "base-choice" or "all-combinations"
SEARCH_STRATEGY="base-choice"
### "" or "--enable-cache"
CACHE_OPTION=""

# Real World Faulty Alloy Models

REAL_FAULTS=(
        # Success.
        "addrFaulty"
        # Success.
        "arr1"
        # Success.
        "arr2"
        # Success.
        "balancedBST1"
        # Failure.
        "balancedBST2"
        # Success.
        "balancedBST3"
        # Failure.
        "bemplFaulty"
        # Success.
        "cd1"
        # Success.
        "cd2"
        # Success.
        "ctreeFaulty"
        # Success.
        "dll1"
        # Success.
        "dll2"
        # Failure.
        "dll3"
        # Success.
        "dll4"
        # Success.
        "fsm1"
        # Success.
        "fsm2"
        # Success.
        "gradeFaulty"
        # Success only by ALL_COMBINATIONS
        "otherFaulty"
        # Success.
        "student1"
        # Failure.  But we should revisit this.  The FL technique fail
        # to locate the fault unless we rank all nodes with
        # suspiciousness score >= 0.
        "student2"
        # Failed.
        "student3"
        # Success.
        "student4"
        # Success.
        "student5"
        # Failure.
        "student6"
        # Failure.
        "student7"
        # Success.
        "student8"
        # Success.
        "student9"
        # Success.
        "student10"
        # Success.
        "student11"
        # Success.
        "student12"
        # Success.
        "student13"
        # Failure.
        "student14"
        # Success.
        "student15"
        # Failure.  Empty model.
        "student16"
        # Failure.
        "student17"
        # Success.
        "student18"
        # Failure.
        "student19"
        # Success.
        "farmerFaulty"
)

declare -g -A addrFaulty=(
        [model_name]="addrFaulty"
        [model_path]="${REAL_BUG_DIR}/addrFaulty.als"
        [correct_model_path]="${MODEL_DIR}/addr.als"
        [scope]="3"
        [test_name]="addr"
        [test_path]="${TEST_DIR}/addr.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A arr1=(
        [model_name]="arr1"
        [model_path]="${REAL_BUG_DIR}/arr1.als"
        [correct_model_path]="${MODEL_DIR}/addr.als"
        [scope]="3"
        [test_name]="arr"
        [test_path]="${TEST_DIR}/arr.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A arr2=(
        [model_name]="arr2"
        [model_path]="${REAL_BUG_DIR}/arr2.als"
        [correct_model_path]="${MODEL_DIR}/addr.als"
        [scope]="3"
        [test_name]="arr"
        [test_path]="${TEST_DIR}/arr.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A balancedBST1=(
        [model_name]="balancedBST1"
        [model_path]="${REAL_BUG_DIR}/balancedBST1.als"
        [correct_model_path]="${MODEL_DIR}/balancedBST.als"
        [scope]="4"
        [test_name]="balancedBST"
        [test_path]="${TEST_DIR}/balancedBST.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A balancedBST2=(
        [model_name]="balancedBST2"
        [model_path]="${REAL_BUG_DIR}/balancedBST2.als"
        [correct_model_path]="${MODEL_DIR}/balancedBST.als"
        [scope]="4"
        [test_name]="balancedBST"
        [test_path]="${TEST_DIR}/balancedBST.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A balancedBST3=(
        [model_name]="balancedBST3"
        [model_path]="${REAL_BUG_DIR}/balancedBST3.als"
        [correct_model_path]="${MODEL_DIR}/balancedBST.als"
        [scope]="4"
        [test_name]="balancedBST"
        [test_path]="${TEST_DIR}/balancedBST.als"
        [minimum_cost]="5"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A bemplFaulty=(
        [model_name]="bemplFaulty"
        [model_path]="${REAL_BUG_DIR}/bemplFaulty.als"
        [correct_model_path]="${MODEL_DIR}/bempl.als"
        [scope]="3"
        [test_name]="bempl"
        [test_path]="${TEST_DIR}/bempl.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A cd1=(
        [model_name]="cd1"
        [model_path]="${REAL_BUG_DIR}/cd1.als"
        [correct_model_path]="${MODEL_DIR}/cd.als"
        [scope]="3"
        [test_name]="cd"
        [test_path]="${TEST_DIR}/cd.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A cd2=(
        [model_name]="cd2"
        [model_path]="${REAL_BUG_DIR}/cd2.als"
        [correct_model_path]="${MODEL_DIR}/cd.als"
        [scope]="3"
        [test_name]="cd"
        [test_path]="${TEST_DIR}/cd.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A ctreeFaulty=(
        [model_name]="ctreeFaulty"
        [model_path]="${REAL_BUG_DIR}/ctreeFaulty.als"
        [correct_model_path]="${MODEL_DIR}/ctree.als"
        [scope]="3"
        [test_name]="ctree"
        [test_path]="${TEST_DIR}/ctree.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A dll1=(
        [model_name]="dll1"
        [model_path]="${REAL_BUG_DIR}/dll1.als"
        [correct_model_path]="${MODEL_DIR}/dll.als"
        [scope]="3"
        [test_name]="dll"
        [test_path]="${TEST_DIR}/dll.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A dll2=(
        [model_name]="dll2"
        [model_path]="${REAL_BUG_DIR}/dll2.als"
        [correct_model_path]="${MODEL_DIR}/dll.als"
        [scope]="3"
        [test_name]="dll"
        [test_path]="${TEST_DIR}/dll.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A dll3=(
        [model_name]="dll3"
        [model_path]="${REAL_BUG_DIR}/dll3.als"
        [correct_model_path]="${MODEL_DIR}/dll.als"
        [scope]="3"
        [test_name]="dll"
        [test_path]="${TEST_DIR}/dll.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A dll4=(
        [model_name]="dll4"
        [model_path]="${REAL_BUG_DIR}/dll4.als"
        [correct_model_path]="${MODEL_DIR}/dll.als"
        [scope]="3"
        [test_name]="dll"
        [test_path]="${TEST_DIR}/dll.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A farmerFaulty=(
        [model_name]="farmerFaulty"
        [model_path]="${REAL_BUG_DIR}/farmerFaulty.als"
        [correct_model_path]="${MODEL_DIR}/farmer.als"
        [scope]="4"
        [test_name]="farmer"
        [test_path]="${TEST_DIR}/farmer.als"
        [minimum_cost]="3"
        [cache_option]=""
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A fsm1=(
        [model_name]="fsm1"
        [model_path]="${REAL_BUG_DIR}/fsm1.als"
        [correct_model_path]="${MODEL_DIR}/fsm.als"
        [scope]="3"
        [test_name]="fsm"
        [test_path]="${TEST_DIR}/fsm.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A fsm2=(
        [model_name]="fsm2"
        [model_path]="${REAL_BUG_DIR}/fsm2.als"
        [correct_model_path]="${MODEL_DIR}/fsm.als"
        [scope]="3"
        [test_name]="fsm"
        [test_path]="${TEST_DIR}/fsm.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A gradeFaulty=(
        [model_name]="gradeFaulty"
        [model_path]="${REAL_BUG_DIR}/gradeFaulty.als"
        [correct_model_path]="${MODEL_DIR}/grade.als"
        [scope]="3"
        [test_name]="grade"
        [test_path]="${TEST_DIR}/grade.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A otherFaulty=(
        [model_name]="otherFaulty"
        [model_path]="${REAL_BUG_DIR}/otherFaulty.als"
        [correct_model_path]="${MODEL_DIR}/other.als"
        [scope]="4"
        [test_name]="other"
        [test_path]="${TEST_DIR}/other.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student1=(
        [model_name]="student1"
        [model_path]="${REAL_BUG_DIR}/student1.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student2=(
        [model_name]="student2"
        [model_path]="${REAL_BUG_DIR}/student2.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student3=(
        [model_name]="student3"
        [model_path]="${REAL_BUG_DIR}/student3.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student4=(
        [model_name]="student4"
        [model_path]="${REAL_BUG_DIR}/student4.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student5=(
        [model_name]="student5"
        [model_path]="${REAL_BUG_DIR}/student5.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student6=(
        [model_name]="student6"
        [model_path]="${REAL_BUG_DIR}/student6.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student7=(
        [model_name]="student7"
        [model_path]="${REAL_BUG_DIR}/student7.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student8=(
        [model_name]="student8"
        [model_path]="${REAL_BUG_DIR}/student8.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student9=(
        [model_name]="student9"
        [model_path]="${REAL_BUG_DIR}/student9.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student10=(
        [model_name]="student10"
        [model_path]="${REAL_BUG_DIR}/student10.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student11=(
        [model_name]="student11"
        [model_path]="${REAL_BUG_DIR}/student11.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student12=(
        [model_name]="student12"
        [model_path]="${REAL_BUG_DIR}/student12.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student13=(
        [model_name]="student13"
        [model_path]="${REAL_BUG_DIR}/student13.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student14=(
        [model_name]="student14"
        [model_path]="${REAL_BUG_DIR}/student14.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student15=(
        [model_name]="student15"
        [model_path]="${REAL_BUG_DIR}/student15.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student16=(
        [model_name]="student16"
        [model_path]="${REAL_BUG_DIR}/student16.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student17=(
        [model_name]="student17"
        [model_path]="${REAL_BUG_DIR}/student17.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student18=(
        [model_name]="student18"
        [model_path]="${REAL_BUG_DIR}/student18.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]="--enable-cache"
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)

declare -g -A student19=(
        [model_name]="student19"
        [model_path]="${REAL_BUG_DIR}/student19.als"
        [correct_model_path]="${MODEL_DIR}/student.als"
        [scope]="3"
        [test_name]="student"
        [test_path]="${TEST_DIR}/student.als"
        [minimum_cost]="3"
        [cache_option]=""
        [max_try_per_hole]="1000"
        [max_partition_num]="10"
        [max_try_num_per_depth]="10000"
)
