<%@ page import="org.mskcc.pathdb.action.BaseAction,
                 org.mskcc.pathdb.protocol.ProtocolRequest,
                 org.mskcc.pathdb.protocol.ProtocolConstantsVersion1,
                 org.mskcc.pathdb.protocol.ProtocolConstantsVersion2,
                 org.mskcc.pathdb.form.WebUIBean,
                 org.mskcc.pathdb.servlet.CPathUIConfig,
                 org.mskcc.pathdb.taglib.Pager,
                 org.mskcc.pathdb.sql.dao.DaoCPath,
                 org.mskcc.pathdb.sql.dao.DaoInternalLink,
                 org.mskcc.pathdb.sql.dao.DaoExternalDbSnapshot,
                 org.mskcc.pathdb.sql.query.QueryUtil,
                 org.mskcc.pathdb.model.CPathRecord,
                 org.mskcc.pathdb.model.CPathRecordType,
                 org.mskcc.pathdb.model.GlobalFilterSettings,
                 org.mskcc.pathdb.model.TypeCount,
                 org.mskcc.pathdb.model.ExternalDatabaseSnapshotRecord,
                 org.mskcc.pathdb.util.html.HtmlUtil,
                 org.mskcc.pathdb.schemas.biopax.summary.BioPaxRecordSummaryUtils,
                 org.mskcc.pathdb.schemas.biopax.summary.BioPaxRecordSummary,
                 org.mskcc.pathdb.util.biopax.BioPaxRecordUtil,
                 org.mskcc.pathdb.taglib.DbSnapshotInfo,
                 org.mskcc.pathdb.taglib.ReactomeCommentUtil,
                 java.net.URLEncoder"%>
<%@ page import="org.mskcc.pathdb.protocol.ProtocolConstants"%>
<%@ page import="org.apache.lucene.search.Hits" %>
<%@ page import="org.apache.lucene.document.Document" %>
<%@ page import="org.apache.lucene.document.Field" %>
<%@ page import="org.mskcc.pathdb.lucene.LuceneConfig" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="/WEB-INF/taglib/cbio-taglib.tld" prefix="cbio" %>
<%@ page errorPage = "../JspError.jsp" %>
<%	request.setAttribute(BaseAction.ATTRIBUTE_TITLE, "Search Results"); %>
<jsp:include page="../../global/redesign/header.jsp" flush="true" />
<%
    final String CYTOSCAPE_HTTP_SERVER = "127.0.0.1:27182";
    final int MAX_SCOREBOARD_WIDTH = 62;
    final int SCOREBOARD_HEIGHT = 6;

    WebUIBean webUIBean = CPathUIConfig.getWebUIBean();
    ProtocolRequest protocolRequest = (ProtocolRequest)
            request.getAttribute(BaseAction.ATTRIBUTE_PROTOCOL_REQUEST);
    long cpathIds[] = (long[])
            request.getAttribute(BaseAction.ATTRIBUTE_CPATH_IDS);
    Integer totalNumHits = (Integer)
            request.getAttribute(BaseAction.ATTRIBUTE_TOTAL_NUM_HITS);
    List<List<String>> fragments = (List<List<String>>) request.getAttribute
            (BaseAction.ATTRIBUTE_TEXT_FRAGMENTS);
    //Set<String> dataSourceSet = (Set<String>) request.getAttribute
    //        (BaseAction.ATTRIBUTE_DATA_SOURCE_SET);
    Map<Long, Set<String>> recordDataSources = (Map<Long, Set<String>>) request.getAttribute
            (BaseAction.ATTRIBUTE_DATA_SOURCES);
    Map<Long, Float> scores = (Map<Long, Float>) request.getAttribute
            (BaseAction.ATTRIBUTE_SCORES);
    ArrayList<Integer> numDescendentsList = (ArrayList<Integer>) request.getAttribute
            (BaseAction.ATTRIBUTE_NUM_DESCENDENTS);
    String organismFlag = request.getParameter(ProtocolRequest.ARG_ORGANISM);
    String keyType = protocolRequest.getEntityType();
    String keyDataSource = (String) request.getParameter(GlobalFilterSettings.NARROW_BY_DATA_SOURCES_FILTER_NAME);
    // server name
    String serverName = (String) request.getServerName();
    serverName = (serverName.indexOf("pathwaycommons") != -1) ? serverName : serverName + ":8080";
    // cytoscape link
    String urlForCytoscapeLink = (String) request.getAttribute("request_url");
    //String urlForCytoscapeLink = ((StringBuffer)request.getRequestURL()).toString();
    urlForCytoscapeLink = urlForCytoscapeLink.substring(7); // remove "http://" from string
    // data source parameter string  to network neighborhood map
    String encodedDataSourceParameter = "";
    GlobalFilterSettings filterSettings =
            (GlobalFilterSettings) request.getSession().getAttribute(GlobalFilterSettings.GLOBAL_FILTER_SETTINGS);
    DaoExternalDbSnapshot daoSnapShot = new DaoExternalDbSnapshot();
    if (filterSettings != null) {
        for (Long snapshotID : (Set<Long>) filterSettings.getSnapshotIdSet()) {
            ExternalDatabaseSnapshotRecord record = daoSnapShot.getDatabaseSnapshot(snapshotID);
            encodedDataSourceParameter += record.getExternalDatabase().getMasterTerm() + ",";
        }
        // snip off last ' '
        encodedDataSourceParameter = encodedDataSourceParameter.replaceAll(",$", "");
        // encode
        encodedDataSourceParameter = URLEncoder.encode(encodedDataSourceParameter, "UTF-8");
    }
%>
<script type="text/javascript">
    //  Toggles details on single row
    function toggleDetails (id) {
        YAHOO.log ("Toggling row with cPathID:  " + id);
        var elements = new Array();
        elements[0] = document.getElementById(id + "_details");
        var current = YAHOO.util.Dom.getStyle(elements[0], 'display');
        YAHOO.log ("Current Display Style is set to:  " + current);
        var toggleImage = document.getElementById (id + "_image");
        if (current == false || current == "none" || current == null) {
            YAHOO.util.Dom.setStyle(elements, 'display', 'inline');
            if (toggleImage != null) {
                toggleImage.innerHTML = "<img src='jsp/images/close.gif'>";
            }
        } else {
            YAHOO.util.Dom.setStyle(elements, 'display', 'none');
            if (toggleImage != null) {
                toggleImage.innerHTML = "<img src='jsp/images/open.gif'>";
            }
        }
    }
</script>
<%!
private String getInspectorButtonHtml (long cPathId) {
	return "<div class='toggle_details'><a "
        + "title='Toggle Record Details' onClick=\"toggleDetails('cpath_" + cPathId
        + "')\"><div id='cpath_" + cPathId + "_image' class='toggleImage'>"
        + "<img src='jsp/images/open.gif'/></div></a>";
}
private String getDetailsHtml (long cPathId, String label, String html) {
    return ("<div id='cpath_" + cPathId + "_" + label +"' class='details'>"
        + html + "</div>");
}
private String getDataSourceHtml(long cPathId, Map<Long,Set<String>> recordDataSources) {
		StringBuffer html = new StringBuffer();
        Set<String> dataSourceSet = recordDataSources.get(cPathId);
        if (dataSourceSet.size() > 0) {
            html.append("<p><b>Data Source(s):</b></p>\n\r");
            html.append("<ul>\n\r");
            // loop here
            for (String dataSource : (Set<String>)recordDataSources.get(cPathId)) {
                html.append("<li>");
                html.append(dataSource);
                html.append("</li>\n\r");
            }
            html.append("</ul>\n\r");
        }
        return html.toString();
}
private String getPathwaySummaryHtml(CPathRecord record,
                                     BioPaxRecordSummary summary,
                                     ProtocolRequest request) {
    // only show pathway summary info
    //if (record.getType() != CPathRecordType.PATHWAY) return "";
    StringBuffer html = new StringBuffer();
    if (summary.getComments() != null) {
        String comments[] = summary.getComments();
	    if (comments.length > 0) {
            String comment = ReactomeCommentUtil.massageComment(comments[0]);
            String paragraphs[] = comment.split("<p>");
		    if (paragraphs.length > 0) {
				html.append("<p><b>Summary:</b></p>\n\r");
                html.append("<p>");
		        for (String term : request.getQuery().split(" ")) {
			        paragraphs[0] = paragraphs[0].replaceAll(term,
				                                             QueryUtil.START_TAG +
                                                             term +
                                                             QueryUtil.END_TAG);
                }
                html.append(paragraphs[0] + "</p>\n\r");
            }
        }
    }
    return html.toString();
}
private String getFragmentsHtml(List<String> fragments, String summaryLabel, String header, int maxLength) {

    // check args
	if (fragments == null || fragments.size() == 0) return "";

	Character period = new Character('.');
    StringBuffer html = new StringBuffer();
	boolean ulAppended = false;
	for (String fragment : fragments) {
	    // create copy of fragment with html stripped out for comparision purposes
		String fragmentCopy = new String (fragment);
		fragmentCopy = fragmentCopy.replaceAll("<b>", "");
		fragmentCopy = fragmentCopy.replaceAll("</b>", "");
		boolean appendPrefix = (Character.isLowerCase(fragmentCopy.charAt(0)) ||
		                        !Character.isLetter(fragmentCopy.charAt(0)));
		boolean appendSuffix = period.compareTo(fragment.charAt(fragment.length()-1)) != 0;
		// if the fragment equals the summaryLabel (header) skip
	    if (fragmentCopy.indexOf(summaryLabel) != -1 && fragmentCopy.length() <= summaryLabel.length()) continue;
		if (fragment.indexOf(QueryUtil.MEMBER_OF) != -1) {
		    fragment += " " + header + ".";
			appendPrefix = appendSuffix = false;
        }
		// write out the html, add "..." in front and back of fragment as needed
		if (!ulAppended) {
		   html.append("<ul>");
		   ulAppended = true;
		}
     	html.append("<li>");
		if (appendPrefix) html.append("... ");
		fragment = HtmlUtil.truncateLongWords(fragment, maxLength);
		fragment = fragment.replaceAll("(?i)(dr)\\.", "$1\\*"); // to prevent Dr. being replade with Dr. ...
		fragment = fragment.replaceAll("\\.", ". ...");
		fragment = fragment.replaceAll("(?i)(dr)\\*", "$1\\."); // to prevent Dr. being replade with Dr. ...
		if (fragment.matches("^.*\\. \\.\\.\\.$")) {
		    fragment = fragment.replaceAll("\\. \\.\\.\\.$", ".");
        }
		html.append(fragment);
		if (appendSuffix) html.append(" ...");
	    html.append("</li>\n\r");
    }
	if (ulAppended) {
	   html.append("</ul>");
	}
    return html.toString();
}
%>
<%
if (protocolRequest.getQuery() != null) {
%>
<h1>Searched for:  <%= protocolRequest.getQuery() %></h1>
<%
}
%>
<%
if (totalNumHits.intValue() == 0) {
%>
	<div class="splitcontentleft">
    <jsp:include page="../../global/redesign/currentFilterSettings.jsp" flush="true" />
	</div>
	<div class="splitcontentright">
<%
	Set<Integer> organismIdSet = filterSettings.getOrganismTaxonomyIdSet();
	if (daoSnapShot.getAllNetworkDatabaseSnapshots().size() != filterSettings.getSnapshotIdSet().size() ||
		!organismIdSet.contains(GlobalFilterSettings.ALL_ORGANISMS_FILTER_VALUE)) {
		out.println("<div class=\"user_message\"><b>Oops.  No Matching Records Found. " +
                "Try updating your filter settings or try a different search term.</b></div></p>");
	}
	else {
		out.println("<div class=\"user_message\"><b>Oops.  No Matching Records Found.  Please try " +
                "a different search term.</b></div>");
	}
%>
    </div>
<%
}
else {
    out.println("<p>");
    String recordStr = "records";
    if (totalNumHits.intValue() == 1) {
        recordStr = "record";
    }
    out.println("Your search has found <b>" + totalNumHits.intValue() + "</b> " +
                "relevant " + recordStr + ":<br>");
%>
	<div class="splitcontentleft">

    <div class="box">
    <h3>Narrow Results</h3>
    <jsp:include page="./narrow-by-type.jsp" flush="true" />
    <jsp:include page="./narrow-by-datasource.jsp" flush="true" />
    </div>
    <jsp:include page="../../global/redesign/currentFilterSettings.jsp" flush="true" />
	</div>
    <div class="splitcontentright">
<%
	Pager pager = new Pager (protocolRequest, totalNumHits.intValue());
	out.println("<div class='search_buttons'>");
	out.println("<srp>" + pager.getHeaderHtmlForSearchPage("white", ProtocolRequest.ARG_ENTITY_TYPE + "=" + keyType + "&" + GlobalFilterSettings.NARROW_BY_DATA_SOURCES_FILTER_NAME + "=" + keyDataSource) + "</srp>");
	out.println ("</div>");
%>
    <table cellpadding="2" cellspacing="0" border="0" width="100%">
<%
    DaoCPath dao = DaoCPath.getInstance();
	DaoInternalLink daoInternalLink = new DaoInternalLink();
    for (int i=0; i< cpathIds.length; i++) {
        CPathRecord record = dao.getRecordById(cpathIds[i]);
        List<TypeCount> childTypes= null;
		// get number children / parents - used to render on hide link to cytoscape
		if (record.getType().toString().equals(CPathRecordType.PHYSICAL_ENTITY)) {
		    Set<Long> snapshotIdSet = filterSettings.getSnapshotIdSet();
		    long snapshotIds[] = new long[snapshotIdSet.size()];
            Iterator<Long> iterator = snapshotIdSet.iterator();
            int index = 0;
            while (iterator.hasNext()) {
                Long snapshotId = iterator.next();
                snapshotIds[index] = snapshotId.longValue();
            }
    		daoInternalLink.getParentTypes(cpathIds[i], record.getNcbiTaxonomyId(),
	    	    snapshotIds, null);
        } else {
		    childTypes = daoInternalLink.getChildrenTypes(cpathIds[i], null);
        }
		boolean showCytoscape = false;
		for (TypeCount typeCount : childTypes) {
			if (typeCount.getCount() > 0) {
				showCytoscape = true;
				break;
			}
		}
        String url = "record2.do?id=" + record.getId();
	    // used to render score board graphic
	    Float score = (Float)scores.get(cpathIds[i]);
	    int scoreboardWidth = (int)(score * MAX_SCOREBOARD_WIDTH);
	    int percentage = (int)(score * 100);
	    // some massaging to handle scores very close to zero, but not zero
	    scoreboardWidth = (percentage == 1 && scoreboardWidth == 0) ? 1 : scoreboardWidth;
	    scoreboardWidth = (score > 0.0 && scoreboardWidth == 0) ? 1 :  scoreboardWidth;
	    String spacerString = (scoreboardWidth < MAX_SCOREBOARD_WIDTH) ?
		    ("<td><img src=\"jsp/images/spacer.gif\" width=\"" + String.valueOf(MAX_SCOREBOARD_WIDTH-scoreboardWidth) + "\" height=\"" + SCOREBOARD_HEIGHT + "\" alt=\"" + String.valueOf(percentage) + "%\"></td>") : "";
		String summaryLabel = "";
		String header = "";
        try {
            BioPaxRecordSummary summary = BioPaxRecordUtil.createBioPaxRecordSummary(record);
			summaryLabel = summary.getLabel();
            header = BioPaxRecordSummaryUtils.getBioPaxRecordHeaderString(summary);
			// if protein, add organism information
			if (record.getType() == CPathRecordType.PHYSICAL_ENTITY) {
			    String organism = summary.getOrganism();
			    if (organism != null && organism.length() > 0) {
				    header += (" [" + organism + "]");
                }
            }
            out.println("<tr valign=\"top\">");
			// score bar
			//out.println("<th align=left>" + String.valueOf(score) + "</th>");
			out.println("<th align=left width=\"" + MAX_SCOREBOARD_WIDTH + "\">");
			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" bgcolor=\"#ffffff\">");
			out.println("<div id='scoreboard'><tr>");
			out.println("<tr><td><img src=\"jsp/images/relevance.gif\" width=\"" +
			            String.valueOf(scoreboardWidth) +
			            "\" height=\"" + SCOREBOARD_HEIGHT + "\" alt=\"" +
			            String.valueOf(percentage) +
			            "%\">" + "</td>" + spacerString);
			out.println("</tr></div>");
		    out.println("</table>");
			out.println("</th>");
			// record header
            out.println("<th align=left width=\"90%\">");
			out.println("<a href=\"" + url + "\">" + header + "</a>");

			//  Show pathway size
			if (record.getType() == CPathRecordType.PATHWAY) {
                if (numDescendentsList != null) {
                    Integer num = numDescendentsList.get(i);
                    String text;
                    if (num == 1) {
                        text = "1 participant";
                    } else {
                           text = num + " participants";
                    }
                    out.println ("&nbsp;&nbsp;<span class='small_no_bold'>[" + text +"]</span>");
                }
            }

            out.println("</th>");
			// inspection button
			out.println("<th align=right>");
			out.println(getInspectorButtonHtml(record.getId()));
			out.println("</th>");
			out.println("</tr>");
			// details
			out.println("<tr><td colspan=\"3\">");
			out.println("<div id='cpath_" + record.getId() + "_details' class='details'>");
			out.println(getPathwaySummaryHtml(record, summary, protocolRequest));
			out.println(getDataSourceHtml(record.getId(), recordDataSources));
			out.println("</div>");
			out.println("</td></tr>");
        } catch (IllegalArgumentException e) {
            out.println("<div>" +
                    "<a href=\"" + url + "\">" + record.getName() + "</a></div>");
        }
        if (organismFlag == null) {
			// fragments
            out.println("<tr><td colspan=\"3\">");
			out.println("<div class='search_fragment'>");
			String htmlFragments = getFragmentsHtml(fragments.get(i), summaryLabel, header, 40);
			if (htmlFragments.length() > 0) {
				out.println("<p><b>Excerpts:</b></p>\n\r");
				out.println(htmlFragments);
			}
			boolean pathwayType = record.getType() == CPathRecordType.PATHWAY;
			if (webUIBean.getWantCytoscape() && (pathwayType && showCytoscape)) {
				// add link to cytoscape
				if (pathwayType) {
					out.println("<a href=\"http://" + CYTOSCAPE_HTTP_SERVER + "/" +
								urlForCytoscapeLink +
								"?" + ProtocolRequest.ARG_VERSION + "=" + ProtocolConstantsVersion2.VERSION_2 +
								"&" + ProtocolRequest.ARG_COMMAND + "=" + ProtocolConstants.COMMAND_GET_RECORD_BY_CPATH_ID +
								"&" + ProtocolRequest.ARG_OUTPUT + "=" + ProtocolConstantsVersion1.FORMAT_BIO_PAX +
								"&" + ProtocolRequest.ARG_QUERY + "=" + String.valueOf(cpathIds[i])  +
								"&" + ProtocolRequest.ARG_DATA_SOURCE + "=" + encodedDataSourceParameter +
								"\"" + " id=\"" + String.valueOf(cpathIds[i]) +"\"" +
								//" onmouseover=\"return overlib(toolTip, WIDTH, 25, FULLHTML, WRAP, CELLPAD, 5, OFFSETY, 0); return true;\"" +
								//" onmouseout=\"return nd();\"" +
								" onclick=\"appRequest(this.href, this.id, " + "'" + ProtocolConstants.COMMAND_GET_RECORD_BY_CPATH_ID + "', " + "'empty_title', '" + encodedDataSourceParameter + "'); return false;\"" +
								">View pathway in Cytoscape</a>");
				}
				else {
					String encodedNeighborhoodTitle = URLEncoder.encode("Neighborhood: " + summaryLabel, "UTF-8");
					out.println("<a href=\"http://" + CYTOSCAPE_HTTP_SERVER + "/" +
								urlForCytoscapeLink +
								"?" + ProtocolRequest.ARG_VERSION + "=" + ProtocolConstantsVersion2.VERSION_2 +
								"&" + ProtocolRequest.ARG_COMMAND + "=" + ProtocolConstantsVersion2.COMMAND_GET_NEIGHBORS +
								"&" + ProtocolRequest.ARG_OUTPUT + "=" + ProtocolConstantsVersion1.FORMAT_BIO_PAX +
								"&" + ProtocolRequest.ARG_QUERY + "=" + String.valueOf(cpathIds[i]) + 
								"&" + ProtocolRequest.ARG_DATA_SOURCE + "=" + encodedDataSourceParameter +
								"&" + ProtocolRequest.ARG_NEIGHBORHOOD_TITLE + "=" + encodedNeighborhoodTitle + "\"" +
								" id=\"" + String.valueOf(cpathIds[i]) +"\"" +
								" onclick=\"appRequest(this.href, this.id, " + "'" + ProtocolConstantsVersion2.COMMAND_GET_NEIGHBORS + "', '" + encodedNeighborhoodTitle + "', '" + encodedDataSourceParameter + "'); return false;\"" +
								">View network neighborhood in Cytoscape</a>");
				}
				out.println("<a href=\"cytoscape.do\">(help)</a>");
			}
			out.println("</div>");
			out.println("</td></tr>");
        }
    }
    out.println("</table>");
	out.println("</div>");
}
%>
<p/>
<jsp:include page="../../global/redesign/footer.jsp" flush="true" />
