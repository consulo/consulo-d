package consulo.d.language.moduleAware;

import consulo.index.io.data.DataExternalizer;
import consulo.index.io.data.DataInputOutputUtil;
import consulo.index.io.data.IOUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DCompileOptionsExternalizer implements DataExternalizer<DCompileOptions> {
    public static final DCompileOptionsExternalizer INSTANCE = new DCompileOptionsExternalizer();

    @Override
    public void save(DataOutput out, DCompileOptions value) throws IOException {
        writeList(out, value.versions());
        writeList(out, value.debugIdentifiers());
    }

    @Override
    public DCompileOptions read(DataInput in) throws IOException {
        List<String> versions = readList(in);
        List<String> debugIdentifiers = readList(in);
        return new DCompileOptions(versions, debugIdentifiers);
    }

    private static void writeList(DataOutput out, List<String> values) throws IOException {
        DataInputOutputUtil.writeINT(out, values.size());
        for (String value : values) {
            IOUtil.writeUTF(out, value);
        }
    }

    private static List<String> readList(DataInput in) throws IOException {
        int count = DataInputOutputUtil.readINT(in);
        List<String> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(IOUtil.readUTF(in));
        }
        return values;
    }
}
