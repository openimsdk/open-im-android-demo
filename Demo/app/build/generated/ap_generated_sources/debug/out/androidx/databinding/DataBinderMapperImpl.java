package androidx.databinding;

public class DataBinderMapperImpl extends MergedDataBinderMapper {
  DataBinderMapperImpl() {
    addMapper(new io.openim.android.demo.DataBinderMapperImpl());
  }
}
