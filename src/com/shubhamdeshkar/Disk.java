package com.shubhamdeshkar;

public class Disk {
    // setting disk capacity to 100M Bytes
    private static final int SIZE = 1048576000;
    // again page table capacity is 100MB divided into chunks of 64 Bytes
    private static final int CAPACITY = 1638400;
    // creating a table for storing addresses
    private int[][] pageTable;

    // constructor for Disk object
    public Disk(int[][] pageTable) {
        if (pageTable.length > 0 && pageTable.length <= CAPACITY) {
            this.pageTable = pageTable;
        }
        System.out.println("initialized disk memory with " + SIZE + " Bytes of memory");
    }

    // function performs search in the container of the L1 cache
    public int searchPhysical(int physicalAddress) {
        System.out.println("disk phys: " + Integer.toHexString(physicalAddress));
        for (int i = 0; i < pageTable.length; i++) {
            // this line creates a physical address from the internal page table and
            // next line compares it with the address to be found
            int combinedAddress = (pageTable[i][0] << 0x10) | (pageTable[i][1]);
            if (combinedAddress == physicalAddress) {
                System.out.println("DISK HIT!");
                return pageTable[i][1];
            }
        }
        System.out.println("DISK MISS!");
        System.out.println("CATASTROPHIC FAILURE!");
        return -1;
    }

    // getter function for getting the page table
    public int[][] getPageTable() {
        return pageTable;
    }
}
