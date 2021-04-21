load Nx.hdl,
output-file Nx.out,
compare-to Nx.cmp,
output-list in%B3.1.3 nx%B3.1.3 out%B3.1.3;

set in 0,
set nx 0,
eval,
output;

set in 0,
set nx 1,
eval,
output;

set in 1,
set nx 0,
eval,
output;

set in 1,
set nx 1,
eval,
output;
