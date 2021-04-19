load Zr16.hdl,
output-file Zr16.out,
compare-to Zr16.cmp,
output-list a%B1.16.1 out%B1.1.1;

set a %B0000000000000000,
eval,
output;

set a %B1111111111111111,
eval,
output;

set a %B1010101010101010,
eval,
output;

set a %B0011110011000011,
eval,
output;

set a %B0001001000110100,
eval,
output;