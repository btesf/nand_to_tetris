load Nx16.hdl,
output-file Nx16.out,
compare-to Nx16.cmp,
output-list in%B1.16.1 nx%B1.1.1 out%B1.16.1;

set in %B0000000000000000,
set nx 0,
eval,
output;

set in %B1111111111111111,
set nx 0,
eval,
output;

set in %B1010101010101010,
set nx 0,
eval,
output;

set in %B0011110011000011,
set nx 0,
eval,
output;

set in %B0001001000110100,
set nx 0,
eval,
output;

set in %B0000000000000000,
set nx 1,
eval,
output;

set in %B1111111111111111,
set nx 1,
eval,
output;

set in %B1010101010101010,
set nx 1,
eval,
output;

set in %B0011110011000011,
set nx 1,
eval,
output;

set in %B0001001000110100,
set nx 1,
eval,
output;