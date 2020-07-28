#!/bin/sh

if ! command -v hyperfine &>/dev/null; then
  echo "Hyperfine not found. download from https://github.com/sharkdp/hyperfine"
  exit
fi
now=$(date "+%Y%m%d-%H%M%S")
script_dir="$(dirname "$(readlink -f "$0")")"
mkdir -p $script_dir/results
output="$script_dir/results/$now-parser-bench.txt"

cur_dir=$(pwd)
ced $script_dir


function execute() {
  echo "benchmarking file $1" | tee -a $output
  hyperfine --warmup 10 "php --syntax-check $1" | tee -a $output
}


base="$script_dir/src/main/resources"
f0="$base/benchmarkgame/fannkuchredux.php-1.php"
f1="$base/benchmarkgame/fasta.php-2.php"
f2="$base/phpoffice/Xls.php"
f3="$base/phpoffice/Comment.php"
f4="$base/symfony/RequestTest.php"
f5="$base/symfony/FrameworkExtension.php"

execute $f0
execute $f1
execute $f2
execute $f3
execute $f4
execute $f5

mvn package
java -jar target/benchmarks.jar org.graalphp.benchmark.BenchmarkParser | tee $output

cd $cur_dir