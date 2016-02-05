# Cypher Writes Test

Test performance of Cypher writes


Start the database, run a test, wait 10 seconds, run it a second time, record that 2nd time.
Stop the database, wipe it, restart it and try a different test.

![alt text](https://github.com/maxdemarzi/cypher_writes_test/raw/master/more-writes.jpg "Cypher Writes compared.")

From 2.3.2 tests we can see the single node creation performance drop is huge. Almost 8k down to 300 when a schema index is added. However batched writes of 1000 nodes at a time only drop from 174k to 52k. So that’s only a 3.3x drop vs 26x drop for single node writes.

Also 52k nodes created with a schema index is better than 8k nodes without.