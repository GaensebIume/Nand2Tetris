# PROJECT 3
Implements memory structures
## Gates
### Bit
holds one bit of storage
requires load bit and value bit
held value is updated if load bit is true
### Register
holds 16 bit of storage
requires load bit and value bit
held value is updated if load bit is true
### RAM8
holds 128 bit of storage
requires load bit and value stream
load bits specify, which RAM address is written to
held value is updated if load bit is true
### RAM64
holds 1024 bit of storage
requires load bit and value stream
load bits specify, which RAM address is written to
held value is updated if load bit is true
### PC
counts number of running programs cycles
requires 16 bit stream and 3 extra bits
increments input stream by one, loads input or resets number according to extra bits
returns new number of programs cycle