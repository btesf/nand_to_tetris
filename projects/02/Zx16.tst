load Zx16.hdl,
output-file Zx16.out,
compare-to Zx16.cmp,
output-list in%B1.16.1 zx%B1.1.1 out%B1.16.1;

set in %B0000000000000000,
set zx 0,
eval,
output;

set in %B0000000000000000,
set zx 0,
eval,
output;

set in %B1111111111111111,
set zx 0,
eval,
output;

set in %B1010101010101010,
set zx 0,
eval,
output;

set in %B0011110011000011,
set zx 0,
eval,
output;

set in %B0001001000110100,
set zx 0,
eval,
output;


set in %B0000000000000000,
set zx 1,
eval,
output;

set in %B0000000000000000,
set zx 1,
eval,
output;

set in %B1111111111111111,
set zx 1,
eval,
output;

set in %B1010101010101010,
set zx 1,
eval,
output;

set in %B0011110011000011,
set zx 1,
eval,
output;

set in %B0001001000110100,
set zx 1,
eval,
output;