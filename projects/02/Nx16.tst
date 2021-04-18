load Nx16.hdl,
output-file Nx16.out,
compare-to Nx16.cmp,
output-list a%B1.16.1 b%B1.1.1 out%B1.16.1;

set a %B0000000000000000,
set b 0,
eval,
output;

set a %B1111111111111111,
set b 0,
eval,
output;

set a %B1010101010101010,
set b 0,
eval,
output;

set a %B0011110011000011,
set b 0,
eval,
output;

set a %B0001001000110100,
set b 0,
eval,
output;

set a %B0000000000000000,
set b 1,
eval,
output;

set a %B1111111111111111,
set b 1,
eval,
output;

set a %B1010101010101010,
set b 1,
eval,
output;

set a %B0011110011000011,
set b 1,
eval,
output;

set a %B0001001000110100,
set b 1,
eval,
output;