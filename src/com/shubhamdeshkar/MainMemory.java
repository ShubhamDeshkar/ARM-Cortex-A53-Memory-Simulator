package com.shubhamdeshkar;

public class MainMemory {
    // size of the main memory is chosen to be 1M Bytes
    private static final int SIZE = 1048576;
    // capacity of the page table is 1MB divided by 64 Bytes
    private static final int CAPACITY = 16834;
    // creating a table for storing page addresses against its tag values
    private int[][] pageTable;

    // constructor for mainMemory object
    public MainMemory(int[][] pageTable) {
        if (pageTable.length > 0 && pageTable.length <= CAPACITY) {
            this.pageTable = pageTable;
        }
        System.out.println("Main Memory initialized with " + SIZE + " Bytes of memory");
    }

    // function performs search in the container of the L1 cache
    public int searchPhysical(int physicalAddress) {
        System.out.println("mainMem. phys: " + Integer.toHexString(physicalAddress));
        for (int i = 0; i < pageTable.length; i++) {
            int combinedAddress = (pageTable[i][0] << 0x10) | (pageTable[i][1]);
            if (combinedAddress == physicalAddress) {
                System.out.println("mainMem. HIT!");
                return pageTable[i][1];
            }
        }
        System.out.println("mainMem. MISS!");
        return -1;
    }

    // this function will be called by the Main() when TLB miss occurs and it is needed to update its
    // page table
    public void update(int physicalAddress) {
        for (int i = 0; i < pageTable.length; i++) {
            if (pageTable[i][0] == 0 && pageTable[i][1] == 0) {
                int mask = 0xFFFF0000;
                int tag = (physicalAddress & mask) >> 0x10;
                // cancelling the effect of arithmetic shift
                tag = tag & ~mask;
                int offset = physicalAddress & ~mask;
                pageTable[i][0] = tag;
                pageTable[i][1] = offset;
                return;
            }
        }
    }

    public int[][] getPageTable() {
        return pageTable;
    }
}
