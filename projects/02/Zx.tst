load Zx.hdl,
output-file Zx.out,
compare-to Zx.cmp,
output-list in%B3.1.3 zx%B3.1.3 out%B3.1.3;

set in 0,
set zx 0,
eval,
output;

set in 0,
set zx 1,
eval,
output;

set in 1,
set zx 0,
eval,
output;

set in 1,
set zx 1,
eval,
output;
