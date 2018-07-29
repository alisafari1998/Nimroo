package ir.sahab.nimroo.serialization;

import com.google.protobuf.InvalidProtocolBufferException;
import ir.sahab.nimroo.model.Link;
import ir.sahab.nimroo.model.Meta;
import ir.sahab.nimroo.model.PageData;
import java.util.ArrayList;

public class PageDataSerializer {

  private static PageDataSerializer ourInstance = new PageDataSerializer();

  public static PageDataSerializer getInstance() {
    return  new PageDataSerializer();//ourInstance;
  }

  private PageDataSerializer() {}

  public byte[] serialize(PageData pageData) {

    PageDataProto.PageData.Builder pageBuilder = PageDataProto.PageData.newBuilder();
    pageBuilder.setUrl(pageData.getUrl());
    pageBuilder.setTitle(pageData.getTitle());
    pageBuilder.setText(pageData.getText());

    PageDataProto.Link.Builder linkBuilder = PageDataProto.Link.newBuilder();
    for (Link link : pageData.getLinks()) {
      linkBuilder.clear();
      linkBuilder.setLink(link.getLink());
      linkBuilder.setAnchor(link.getAnchor());
      pageBuilder.addLinks(linkBuilder.build());
    }

    PageDataProto.Meta.Builder metaBuilder = PageDataProto.Meta.newBuilder();
    for (Meta meta : pageData.getMetas()) {
      metaBuilder.clear();
      metaBuilder.setName(meta.getName());
      metaBuilder.setContent(meta.getContent());
      metaBuilder.setCharset(meta.getCharset());
      metaBuilder.setHttpEquiv(meta.getHttpEquiv());
      metaBuilder.setScheme(meta.getScheme());
      pageBuilder.addMetas(metaBuilder.build());
    }
    PageDataProto.PageData protoPageData = pageBuilder.build();
    return protoPageData.toByteArray();
  }

  public PageData deserialize(byte[] input) throws InvalidProtocolBufferException {
    PageData pageData = new PageData();
    PageDataProto.PageData protoPageData = PageDataProto.PageData.parseFrom(input);
    pageData.setUrl(protoPageData.getUrl());
    pageData.setTitle(protoPageData.getTitle());
    pageData.setText(protoPageData.getText());

    ArrayList<Link> links = new ArrayList<>();
    for (PageDataProto.Link protoLink : protoPageData.getLinksList()) {
      Link link = new Link();
      link.setAnchor(protoLink.getAnchor());
      link.setLink(protoLink.getLink());
      links.add(link);
    }
    pageData.setLinks(links);

    ArrayList<Meta> metas = new ArrayList<>();
    for (PageDataProto.Meta protoMeta : protoPageData.getMetasList()) {
      Meta meta = new Meta();
      meta.setName(protoMeta.getName());
      meta.setScheme(protoMeta.getScheme());
      meta.setCharset(protoMeta.getCharset());
      meta.setContent(protoMeta.getContent());
      meta.setHttpEquiv(protoMeta.getHttpEquiv());
      metas.add(meta);
    }
    pageData.setMetas(metas);

    return pageData;
  }
}
