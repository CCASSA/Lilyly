"""Check ELF64 LOAD segments for 16 KiB alignment in the given AAB. No SDK required."""
from pathlib import PurePosixPath
import struct, sys, zipfile
with zipfile.ZipFile(sys.argv[1]) as bundle:
    for member in bundle.namelist():
        p=PurePosixPath(member)
        if not member.endswith('.so') or p.parent.name not in {'arm64-v8a','x86_64'}: continue
        raw=bundle.read(member)
        if raw[:6]!=b'\x7fELF\x02\x01': raise ValueError('Unexpected ELF format: '+member)
        offset=struct.unpack_from('<Q',raw,32)[0]
        size,count=struct.unpack_from('<HH',raw,54)
        loads=[]
        for i in range(count):
            kind,flags,file_off,addr,_,_,_,align=struct.unpack_from('<IIQQQQQQ',raw,offset+size*i)
            if kind==1:
                if align<16384 or (addr-file_off)%16384: raise ValueError('Unaligned LOAD: '+member)
                loads.append(align)
        if not loads: raise ValueError('No loadable segments: '+member)
        print(member, 'LOAD alignment:', *loads)
