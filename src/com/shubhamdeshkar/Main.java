package com.shubhamdeshkar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // creating respective pageTables/data-tables to initialize the entities
        int[][] tlbPageTable1 = createPageTableFrom("TLBData.txt", 10);
        int[][] tlbPageTable2 = createPageTableFrom("TLBInst.txt", 10);
        int[][] l1PageTable1 = createPageTableFrom("L1Data.txt", 128);
        int[][] l1PageTable2 = createPageTableFrom("L1Inst.txt", 128);
        int[][] tlbPageTableUnified = createPageTableFrom("TLBDataUnified.txt", 512);
        int[][] l2PageTable = createPageTableFrom("L2data.txt", 2048);
        int[][] mainMemoryTable = createPageTableFrom("mainMemoryData.txt", 16384);
        // computer could not handle capacity of 1683400, hence it was shortened 100 times
        int[][] diskMemoryTable = createPageTableFrom("diskData.txt", 16384);

        // creating all the required objects
        Disk        disk        = new Disk(diskMemoryTable);
        MainMemory  mainMemory  = new MainMemory(mainMemoryTable);
        L2Cache     l2Cache     = new L2Cache(l2PageTable);
        TLB         tlbUnified  = new TLB(tlbPageTableUnified);
        L1Cache     l1CacheData = new L1Cache(l1PageTable1);
        L1Cache     l1CacheInst = new L1Cache(l1PageTable2);
        TLB         muTLBData   = new TLB(tlbPageTable1);
        TLB         muTLBInst   = new TLB(tlbPageTable2);
        System.out.println("--------------------------------------------------------");

        // necessary initializations here
        int hit = 0;
        int miss = 0;
        int clock = 0;
        // initializing the program counter to 0x5 (101) [randomly chosen]
        int PC = 0x0005;
        int virtualAddData;
        int virtualAddInst;

        try {
            String lineData, lineInst;
            // creating buffer for reading a virtual program
            FileReader fileReader = new FileReader("virtualProgram.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // these lineData and lineInst attempt to read 2 addresses, one for Data and
            // one for instruction
            lineData = bufferedReader.readLine();
            lineInst = bufferedReader.readLine();
            while (lineData != null && lineInst != null) {
                // incrementing program counter by 16 Bytes every program instruction
                PC += 0x10;
                System.out.println("PC: " + Integer.toHexString(PC));

                // interprets the hex format written data into integer
                virtualAddData = Integer.decode(lineData);
                virtualAddInst = Integer.decode(lineInst);

                // attempt to translate the virtual address for Data through the micro TLB
                int phyAddData = muTLBData.getPhysicalAddress(virtualAddData);
                if (phyAddData != -1) {
                    System.out.println("TLB HIT");
                    hit++;
                    System.out.println("physical address generated from micro-TLB: " + Integer.toHexString(phyAddData));
                }
                // if still not found, it will be search in next level TLB, the Unified TLB
                if (phyAddData == -1) {
                    System.out.println("TLBdata MISS!");
                    miss++;
                    // the system is designed such that it TLB-unified HIT will occur 100%
                    // hence not accounting for Page Fault
                    phyAddData = tlbUnified.getPhysicalAddress(virtualAddData);
                    // write back for micro TLB
                    muTLBData.update(phyAddData);
                    System.out.println("physical address from level2 TLB: " + Integer.toHexString(phyAddData));
                }

                // incrementing PC for next address
                PC += 0x10;
                System.out.println("PC: " + Integer.toHexString(PC));
                // attempt to translate the virtual address for Instruction through the micro TLB
                int phyAddInst = muTLBInst.getPhysicalAddress(virtualAddInst);
                if (phyAddInst != -1) {
                    System.out.println("physical address generated from micro-TLB: " + Integer.toHexString(phyAddInst));
                    System.out.println("TLB HIT");
                    hit++;
                }
                // same as above
                if (phyAddInst == -1) {
                    System.out.println("TLBdata MISS!");
                    miss++;
                    phyAddInst = tlbUnified.getPhysicalAddress(virtualAddInst);
                    // write back for micro TLB
                    muTLBInst.update(phyAddInst);
                    System.out.println("physical address from level2 TLB: " + phyAddInst);
                }

                System.out.println("--------------------------------------------------------");

                // Physical address must be generated by now
////////////////////////////////////////////////////////////////////////////////////////////////////////////

                System.out.println("**DATA**");
                // searching the acquired physical address in the L1 cache first. It must return -1
                // if not found in there. (L1 CACHE MISS)
                int statusData = l1CacheData.searchPhysical(phyAddData);
                if (statusData != -1) {
                    clock += 4;
                    hit++;
                }
                else {
                    miss++;
                    // clocks for L1
                    clock += 4;
                    // clocks for L2
                    clock += 8;
                    statusData = l2Cache.searchPhysical(phyAddData);
                    if (statusData == -1) {
                        // clocks for main memory
                        clock += 100;
                        miss++;
                        statusData = mainMemory.searchPhysical(phyAddData);
                        if (statusData == -1) {
                            // clocks for disk
                            clock += 100000;
                            miss++;
                            statusData = disk.searchPhysical(phyAddData);
                            // the system is designed such that disk will hit 100%
                            hit++;
                            // when found in Disk, update in all the top hierarchies
                            mainMemory.update(phyAddData);
                            l2Cache.update(phyAddData);
                            l1CacheData.update(phyAddData);
                        }
                        else {
                            // if found in main memory, write back in L1 and L2
                            l2Cache.update(phyAddData);
                            l1CacheData.update(phyAddData);
                            hit++;
                        }
                    }
                    else {
                        // if found in L2 write back in L1
                        l1CacheData.update(phyAddData);
                        hit++;
                    }
                }
                System.out.println("Data found: " + Integer.toHexString(statusData));

                // searching the acquired physical address in the L1 cache first. It must return -1
                // if not found in there. (L1 CACHE MISS)
                System.out.println("**INST**");
                int statusInst = l1CacheInst.searchPhysical(phyAddInst);
                if (statusInst != -1) {
                    clock += 4;
                    hit++;
                }
                else {
                    // clocks for L1
                    clock += 4;
                    // clocks for L2
                    clock += 8;
                    miss++;
                    statusInst = l2Cache.searchPhysical(phyAddInst);
                    if (statusInst == -1) {
                        // clock for main memory
                        clock += 100;
                        miss++;
                        statusInst = mainMemory.searchPhysical(phyAddInst);
                        if (statusInst == -1) {
                            // clocks for disk
                            clock += 100000;
                            miss++;
                            statusInst = disk.searchPhysical(phyAddInst);
                            // the system is designed such that disk will hit 100%
                            hit++;
                            // when found in Disk, update in all the top hierarchies
                            mainMemory.update(phyAddInst);
                            l2Cache.update(phyAddInst);
                            l1CacheInst.update(phyAddInst);
                        }
                        else {
                            // if found in main memory, write back in L1 and L2
                            l2Cache.update(phyAddInst);
                            l1CacheInst.update(phyAddInst);
                            hit++;
                        }
                    }
                    else {
                        // if found in L2 write back in L1
                        l1CacheData.update(phyAddData);
                        hit++;
                    }
                }
                // printing thr final instruction variable found at the address
                System.out.println("Instruction found: " + Integer.toHexString(statusInst));

                // reading next two lines from the 'virtual program'
                lineData = bufferedReader.readLine();
                lineInst = bufferedReader.readLine();
            }
        }
        catch (IOException e) {
            System.out.println(e.toString());
        }

        // reporting some statistics collected
        System.out.println("==================");
        System.out.println("CLKs: " + clock);
        System.out.println("HITs: " + hit);
        System.out.println("MISS: " + miss);
        System.out.println("==================");

        System.out.println("\n\nPrinting Updated page tables/memories of: \n");
        System.out.println("microTLB data");
        printPageTable(muTLBData.getPageTable());
        System.out.println("microTLB inst");
        printPageTable(muTLBInst.getPageTable());
        System.out.println("L1 cache data");
        printPageTable(l1CacheData.getPageTable());
        System.out.println("L1 cache inst");
        printPageTable(l1CacheInst.getPageTable());
        System.out.println("L2 cache");
        printPageTable(l2Cache.getPageTable());
        System.out.println("main memory");
        printPageTable(mainMemory.getPageTable());
    }


    // this is a helper function that creates page table in the form of 2D integer array
    // which is read from a text file which is treated as an input file for the entity
    private static int[][] createPageTableFrom(String filename, int capacity) {
        int[][] returnArray = new int[capacity][capacity];
        int tag;
        int offset;

        // using try and catch blocks for IOException handling (compulsory by Java)
        try {
            // creating a file reader object to iterate & collect input file text
            FileReader fileReader = new FileReader(filename);
            // creating a buffered reader object to collect and iterate over file-reader object
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // reading first line only before the loop starts checking for EOF
            String line = bufferedReader.readLine();
            if (line != null) {
                // converting first hex string to tag integer to put in the table at (0, 0)
                tag = Integer.decode(line.split(" ")[0]);
                // converting second hex string to offset integer to put in the table at (0, 1)
                offset = Integer.decode(line.split(" ")[1]);
                // initializing first row of the page table to be constructed and returned
                returnArray[0][0] = tag;
                returnArray[0][1] = offset;
            }
            else {
                System.out.println("file could be empty");
            }

            // continuing reading until EOF
            for (int i = 1; i < returnArray.length; i++) {
                line = bufferedReader.readLine();
                if (line != null) {
                    // splitting the read line from buffer into a tag value and offset value
                    // and interpreting that value using the decode function
                    tag = Integer.decode(line.split(" ")[0]);
                    offset = Integer.decode(line.split(" ")[1]);
                    // setting the values of tag and offset into the returnArray
                    returnArray[i][0] = tag;
                    returnArray[i][1] = offset;
                }
            }
        }
        catch (IOException e) {
            System.out.println(e.toString());
        }
        // returning the returnArray which will be used as a page table mock-up by an entity
        return returnArray;
    }

    // another helper function for printing 2D arrays like a page table
    private static void printPageTable(int[][] pageTable) {
        for (int i = 0; i < pageTable.length; i++) {
            if (pageTable[i][0] != 0 && pageTable[i][1] != 0)
                System.out.println(Integer.toHexString(pageTable[i][0]) + " " + Integer.toHexString(pageTable[i][1]));
        }
        System.out.println("\n");
    }
}
