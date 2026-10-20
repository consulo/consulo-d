uint cpuid(uint leaf)
{
    uint a, b;
    asm @trusted nothrow
    {
        "cpuid" : "=a" (a), "=b" (b) : "a" (leaf) : "ecx", "edx";
    }
    return a;
}

void basic()
{
    asm { "nop"; }
    asm { `pause`; }
}

int named(int x)
{
    int r;
    asm
    {
        "movl %[val], %[res]\n\t"
        : [res] "=r" (r)
        : [val] "r" (x)
        : "memory";
    }
    return r;
}

void jump(int x)
{
    asm
    {
        "testl %0, %0; jz %l1" : : "r" (x) : "cc" : done;
    }
done:
    return;
}
