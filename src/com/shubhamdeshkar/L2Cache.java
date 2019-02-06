package com.shubhamdeshkar;

public class L2Cache {
    //  setting size of L2 cache to 128K Bytes
    private static final int SIZE = 131072;
    //  setting max capacity to 128 pages of 64 Bytes
    private static final int CAPACITY = 2048;
    //  container for holding addresses entries
    private int[][] pageTable;

    //  constructor for calling L2 cache objects
    public L2Cache(int[][] pageTable) {
        if (pageTable.length > 0 && pageTable.length <= CAPACITY) {
            this.pageTable = pageTable;
        }
        System.out.println("initialized L2 cache with " + SIZE + " Bytes of memory");
    }

    //  function for searching the addresses in the L2 cache container
    public int searchPhysical(int physicalAddress) {
        System.out.println("L2 phys: " + Integer.toHexString(physicalAddress));
        for (int i = 0; i < pageTable.length; i++) {
            int combinedAddress = (pageTable[i][0] << 0x10) | (pageTable[i][1]);
            if (combinedAddress == physicalAddress) {
                System.out.println("L2 CACHE HIT!");
                return pageTable[i][1];
            }
        }
        System.out.println("L2 CACHE MISS!");
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

    //  getter for the container
    public int[][] getPageTable() {
        return pageTable;
    }
}
