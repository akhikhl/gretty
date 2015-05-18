jQuery.noConflict();

jQuery(function($) {
  $('#sendRequest1').click(function() {
    $('#result1').addClass('hide');
    $.ajax({ type: 'POST', url: '/spring-boot-webservice1/mycontroller/getdate', dataType: 'json' }).done(function(data) {
      $('#result1').removeClass('hide bg-danger').addClass('bg-success').text('Got from spring-boot-webservice1: ' + data.date);
    }).fail(function(jqXHR, textStatus, errorThrown) {
      $('#result1').removeClass('hide bg-success').addClass('bg-danger').text('error: ' + errorThrown);
    });
  });
  $('#sendRequest2').click(function() {
    $('#result2').addClass('hide');
    $.ajax({ type: 'POST', url: '/spring-boot-webservice2/mycontroller/getdate', dataType: 'json' }).done(function(data) {
      $('#result2').removeClass('hide bg-danger').addClass('bg-success').text('Got from spring-boot-webservice2: ' + data.date);
    }).fail(function(jqXHR, textStatus, errorThrown) {
      $('#result2').removeClass('hide bg-success').addClass('bg-danger').text('error: ' + errorThrown);
    });
  });
  $('#sendRequest3').click(function() {
    $('#result3').addClass('hide');
    $.ajax({ type: 'POST', url: '/jee-webservice/myservlet/getdate', dataType: 'json' }).done(function(data) {
      $('#result3').removeClass('hide bg-danger').addClass('bg-success').text('Got from jee-webservice: ' + data.date);
    }).fail(function(jqXHR, textStatus, errorThrown) {
      $('#result3').removeClass('hide bg-success').addClass('bg-danger').text('error: ' + errorThrown);
    });
  });
});
