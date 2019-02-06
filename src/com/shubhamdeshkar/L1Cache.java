package com.shubhamdeshkar;

public class L1Cache {
    //  setting size of the L1 cache to 8K Bytes
    private static final int SIZE = 8192;
    //  total page (addresses) storing capacity is 128
    private static final int CAPACITY = 128;
    //  container for actual pages
    private int[][] pageTable;

    //  constructor for creating an object
    public L1Cache(int[][] pageTable) {
        if (pageTable.length > 0 && pageTable.length <= CAPACITY) {
            this.pageTable = pageTable;
        }
        System.out.println("initialized L1 cache with " + SIZE + " Bytes of memory");
    }

    //  function performs search in the container of the L1 cache
    public int searchPhysical(int physicalAddress) {
        System.out.println("L1 phys: " + Integer.toHexString(physicalAddress));
        for (int i = 0; i < pageTable.length; i++) {
            // this extra processing was done because of the way the page table was designed
            // page table is a 2D array of integers originally designed to perform some function
            // but it quickly ran out of its use and it was difficult to make changes in every
            // components which used page table variable
            int combinedAddress = (pageTable[i][0] << 0x10) | (pageTable[i][1]);
            if (combinedAddress == physicalAddress) {
                System.out.println("L1 CACHE HIT!");
                return pageTable[i][1];
            }
        }
        System.out.println("L1 CACHE MISS!");
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


    //  getter function for getting the contents of the L1 cache
    public int[][] getPageTable() {
        return pageTable;
    }

}
