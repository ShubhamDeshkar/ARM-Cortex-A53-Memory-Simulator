package com.shubhamdeshkar;

public class TLB {
    private int[][] pageTable;

    // constructor for TLB object
    public TLB(int[][] pageTable) {
        this.pageTable = pageTable;
        System.out.println("TLB initialized with " + pageTable.length + " entries");
    }

    // this is a private function created for looking up in the internal page
    // table
    private int searchTLB(int virtualAddress) {
        System.out.println("TLB received: " + Integer.toHexString(virtualAddress));
        int mask = 0xFFFF0000;
        // after extracting the value of tag it is shifted by 16 bits
        int tag = (virtualAddress & mask) >> 0x10;
        System.out.print("TLB tag: " + Integer.toHexString(tag));

        for (int i = 0; i < pageTable.length; i++) {
            if (tag == pageTable[i][0]) {
                System.out.println(" " + Integer.toHexString(tag) + " found!");

                // returns page address from the second column of the page table
                return pageTable[i][1];
            }
        }
        System.out.println(" " + Integer.toHexString(tag) + " not found in TLB");
        return -1;
    }

    public int getPhysicalAddress(int virtualAddress) {
        // searching for the tag in internal page table
        int pageAddress = searchTLB(virtualAddress);

        if (pageAddress != -1) {
            // taking last 16-bits as offset value for the physical address
            int mask = 0x0000FFFF;
            int offset = virtualAddress & mask;

            System.out.print("pageAdd: " + Integer.toHexString(pageAddress));
            System.out.println(" offset: " + Integer.toHexString(offset));
            return (pageAddress << 0x10) | offset;
        }
        // if not found return -1
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

    // getter function for getting the page table
    public int[][] getPageTable() {
        return pageTable;
    }
}
