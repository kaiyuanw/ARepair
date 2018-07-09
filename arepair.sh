#!/usr/bin/env bash

trap "exit" INT

_AREPAIR_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

. ${_AREPAIR_DIR}/models.sh

# Main functionality

function arepair_build() {
        mvn clean package
}

### ARepair.

function arepair_run() {
        java -Xmx16g -Xmx16g -Djava.library.path="${_AREPAIR_DIR}/sat-solvers" -cp "${JAR_PATHS}" patcher.Patcher "$@"
}

function arepair_run_model() {
        eval $(obj.unpack "${1}"); shift
        local meta_dir="${META_DIR}"
        local patch_dir="${RESULT_DIR}"
        mkdir -p "${HIDDEN_DIR}" "${meta_dir}" "${patch_dir}"
        echo "Repairing ${obj[model_name]}"
        arepair_run --model-path "${obj[model_path]}" --test-path "${obj[test_path]}" --scope "${obj[scope]}" --minimum-cost "${obj[minimum_cost]}" --search-strategy "${SEARCH_STRATEGY}" "${CACHE_OPTION}" &> "${meta_dir}/${obj[model_name]}.txt"
        cp "${FIXED_MODEL_PATH}" "${patch_dir}/${obj[model_name]}.als"
}

function model.foreach() {
        local fun="${1}"; shift
        declare -a array=("${!1}"); shift
        for model in ${array[@]}; do
	        ${fun} "$(declare -p ${model})" "$@"
        done
}

function obj.unpack() {
        local aa="${1}"; shift
        echo "declare -A obj="${aa#*=}
}

# ----------
# Main.

case $1 in
        # Build project.
        --build) shift;
	        arepair_build "$@";;
        --run) shift;
                arepair_run "$@";;
        --run-all) shift;
                model.foreach arepair_run_model REAL_FAULTS[@] "$@";;
        *)
	        echo "ERROR: Incorrect arguments: $@"
	        exit 1;;
esac
