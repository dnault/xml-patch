package com.github.dnault.xmlpatch.ant;

import java.io.Reader;
import java.io.StringReader;

import com.github.dnault.xmlpatch.filter.XmlPatch;
import org.apache.tools.ant.filters.BaseFilterReader;
import org.apache.tools.ant.filters.ChainableReader;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Parameterizable;

public class AntXmlPatchFilter extends XmlPatch implements ChainableReader, Parameterizable {
    /**
     * Constructor for "dummy" instances.
     *
     * @see BaseFilterReader#BaseFilterReader()
     */
    @SuppressWarnings("unused")
    public AntXmlPatchFilter() {
        super(new StringReader(""));
        org.apache.tools.ant.util.FileUtils.close(this);
    }

    public AntXmlPatchFilter(Reader in) {
        super(in);
    }

    @Override
    public Reader chain(Reader reader) {
        AntXmlPatchFilter r = new AntXmlPatchFilter(reader);
        r.setPatch(getPatch());
        return r;
    }

    @Override
    public void setParameters(Parameter[] parameters) {
        if (parameters != null) {
            for (Parameter p : parameters) {
                if (p.getName().equalsIgnoreCase("patch")) {
                    setPatch(p.getValue());
                } else {
                    throw new RuntimeException("unrecognized parameter: " + p.getName());
                }
            }
        }
    }
}
