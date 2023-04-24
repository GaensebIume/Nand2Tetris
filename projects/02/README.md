# PROJECT 2
Implements advanced computation gates 

## File naming:
xxx.hdl	$\to$ basic gate <br>
xxxyy.hdl $\to$  gate for yy-long input streams <br>
xxxzzWay.hdl $\to$  gate for zz different input streams<br>

## Gates:
### HalfAdder
requires 2 input values
returns sum of the inputs + one carry bit
### FullAdder
requires 3 input values, two for adding and one carry
returns sum of the inputs + one carry bit
### Inc:
requires 1 input values
returns input value incremented by 1
### ALU
requires 2 16 bit input streams and 6 selection bits
#### selection bits are used to:
set inputs to zero <br>
negate inputs <br>
negate the output <br>
return sum of inputs
<br>
returns modified input stream (according to selection bits) and two bits.
These extra bits are two if output is zero and output is negative respectively.