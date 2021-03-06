<#escape x as x?html>
<@page title="Time Series">


<#-- SECTION Time series search -->
<@section title="Time series search" if=searchRequest??>
  <@form method="GET" action="${uris.allTimeSeries()}">
  <p>
    <@rowin label="Identifier"><input type="text" size="30" maxlength="80" name="identifier" value="${searchRequest.identifierValue}" /></@rowin>
    <@rowin label="Data source"><input type="text" size="30" maxlength="80" name="dataSource" value="${searchRequest.dataSource}" /></@rowin>
    <@rowin label="Data provider"><input type="text" size="30" maxlength="80" name="dataProvider" value="${searchRequest.dataProvider}" /></@rowin>
    <@rowin label="Data field"><input type="text" size="30" maxlength="80" name="dataField" value="${searchRequest.dataField}" /></@rowin>
    <@rowin label="Observation time"><input type="text" size="30" maxlength="80" name="observationTime" value="${searchRequest.observationTime}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Time series results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.documents paging=paging empty="No time series" headers=["Reference","Identifiers","Data source","Data provider","Data field","Observation time","Actions"]; item>
      <td><a href="${uris.oneTimeSeries(item)}">${item.series.uniqueId.value}</a></td>
      <td>
      <#list item.series.identifiers.iterator() as item>
    	   ${item}<br>
   	  </#list>
      </td>
      <td>${item.series.dataSource}</td>
      <td>${item.series.dataProvider}</td>
      <td>${item.series.dataField}</td>
      <td>${item.series.observationTime}</td>
      <td><a href="${uris.oneTimeSeries(item)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>


<#-- SECTION Add timeseries -->
<@section title="Add timeseries" if=searchRequest??>
  <@form method="POST" action="${uris.allTimeSeries()}">
  <p>
    <@rowin label="Scheme type">
      <select name="idscheme">
        <option value="BLOOMBERG_TICKER">BLOOMBERG_TICKER</option>
        <option value="BLOOMBERG_TCM">Bloomberg Ticker/Coupon/Maturity</option>
        <option value="BLOOMBERG_BUID">BLOOMBERG_BUID</option>
        <option value="CUSIP">CUSIP</option>
        <option value="ISIN">ISIN</option>
        <option value="RIC">RIC</option>
        <option value="SEDOL1">SEDOL1</option>
      </select>
    </@rowin>
    <@rowin label="Data provider"><input type="text" size="30" maxlength="80" name="dataProvider" value="" /></@rowin>
    <@rowin label="Data field"><input type="text" size="30" maxlength="80" name="dataField" value="" /></@rowin>
    <@rowin label="Start date(yyyy-mm-dd)"><input type="text" size="30" maxlength="80" name="start" value="" /></@rowin>
    <@rowin label="End date(yyyy-mm-dd)"><input type="text" size="30" maxlength="80" name="end" value="" /></@rowin>
    <@rowin label="Identifiers"></@rowin>
    <@rowin><textarea name="idvalue" cols="35" rows="10"></textarea></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
