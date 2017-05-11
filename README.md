# UCITS
Converting an Index to a UCITS III compliant constrained weights index

***Introduction***

The UCITS III directive imposes certain investment limits to funds originating in member states of the European  Union.This article describes this in more detail and presents a software solution which will convert an existing non - compliant index into a UCITS III constrained weights compliant one.

In a nutshell - with respect to constrained weights - UCITS III states that any single entity in an index should be no more than 10 % of the overall weight of the index. Further, the total weight of all entities with a weight > 5 % should be no more that 40 % of the total weight of the index.
Note that in this context an index "entity" can consist of more than one index member if the members belong to the same "line".

In practice, the above constraints would lead to constant rebalancing and turnover of the index therefore most leading index providers typically use a 10 % buffer on the above constraints to minimize this. In other words, in practice the constraints become:-

No individual entity > 9%   and
The sum of the weights of all entities > 4.5% must be <= 36% of the total index weight.

Given the above conditions we see immediately that the source index we wish to convert to UCITS III compliance must have at least 19 members. If there are only 18 entities typically the 10% buffer mentioned above will be reduced to, say, 9%. 17 entities will reduce it to 4% and 16 members to 0% .
Indices containing fewer than 16 entities cannot be converted to a UCITS III compliant index.

***Methodology***

The process of creating a UCITS III compliant index is essentially iterative. We try all valid combinations of weights within the UCITS guidelines and test each combination for UCITS III compliance.
For large indices it’s likely that there will be several hundred if not thousands of combinations that will be UCITS III compliant. In order to choose which one becomes ***thee*** UCITS III index we compare it to the original index and determine its tracking error. The one with the lowest tracking error becomes our new UCITS III compliant index.
In programming terms we logically divide our original index into 3 pivot points after sorting it by index weight - records 1 to N.

CAP PIVOT - points to the lowest entity with weight = 9 %

HIGH PIVOT - points to the highest entity with weight = 4.5 %

LOW PIVOT - points o the lowest entity with weight = 4.5 %

The entities between the CAP PIVOT point and HIGH PIVOT point are designated HIGH_CAPS. The entities below the LOW PIVOT point are designated LOW_CAPS.
The weights of the entities between and including index entity 1 and the CAP PIVOT and between the HIGH PIVOT and LOW PIVOT points are fixed until the next iteration.
By setting the three pivot points, some weights are then fixed at the 9 % and 36 % limits. Allocation of the weight differences between the original index and the capped weights is applied on a proportional basis to the LOW_CAPS and HIGH_CAPS. When this has been done we check for the 36 % limit again. Any overweight in this must be removed proportionally from the HIGH_CAPS and allocated to the LOW_CAPS. Finally a check is made that no further breaches have occurred. If they have we discard the solution and we move to the next iteration. If not save this solution and move to the next iteration. At the end, check all saved solutions against the original index.

The solution with the lowest tracking error becomes our UCITS III compliant index.

***Software solution***

The software is written in JAVA and was written on a PC using the Eclipse Luna IDE. The target system was an OpenVMS box running an Oracle RDBMS. Database connection and IP details in the code have been changed to protect the innocent, so please use your own. Also there are several references to database tables used that you won’t have on your system. It should be reasonably clear what these tables are used for and can mostly be ignored if you want (see comments in the code for sections that can be skipped).

